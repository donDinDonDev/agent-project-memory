#!/usr/bin/env bash
set -euo pipefail

MAIN_CLASS="io.github.dondindondev.agentprojectmemory.Main"

usage() {
  cat <<'USAGE'
Usage: release-artifact-integrity-dry-run.sh [--project-root <path>] [--asset-dir <path>]

Validate the local release jar candidate and a filename-only SHA256SUMS file.

Options:
  --project-root <path>  Repository root. Defaults to the parent of this script.
  --asset-dir <path>     Dry-run asset directory under <project-root>/target/ with a
                         release-artifact-dry-run name. Defaults to
                         target/release-artifact-dry-run.
  --help                 Show this help.
USAGE
}

fail() {
  printf 'Artifact integrity dry-run failed: %s\n' "$1" >&2
  exit 1
}

normalize_absolute_path() {
  local path="$1"
  [[ "$path" == /* ]] || fail "path must be absolute"

  local old_ifs="$IFS"
  local -a parts
  IFS='/' read -r -a parts <<< "$path"
  IFS="$old_ifs"

  local normalized=""
  local part
  for part in "${parts[@]}"; do
    case "$part" in
      ""|.) ;;
      ..)
        [[ -n "$normalized" ]] || fail "path must not escape filesystem root"
        normalized="${normalized%/*}"
        ;;
      *)
        normalized="${normalized}/${part}"
        ;;
    esac
  done

  [[ -n "$normalized" ]] || normalized="/"
  printf '%s' "$normalized"
}

resolve_input_path() {
  local path="$1"
  case "$path" in
    /*) path="$(normalize_absolute_path "$path")" ;;
    *) path="$(normalize_absolute_path "$(pwd -P)/${path}")" ;;
  esac

  local existing="$path"
  local suffix=""
  while [[ ! -e "$existing" ]]; do
    [[ "$existing" != "/" ]] || break
    suffix="/$(basename "$existing")${suffix}"
    existing="$(dirname "$existing")"
  done

  existing="$(cd "$existing" && pwd -P)"
  normalize_absolute_path "${existing}${suffix}"
}

ensure_under_target() {
  local path="$1"
  case "$path" in
    "${target_dir}/"*) ;;
    *)
      fail "asset directory must be under target/"
      ;;
  esac
}

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
project_root="$(cd "${script_dir}/.." && pwd -P)"
asset_dir=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --project-root)
      [[ $# -ge 2 ]] || fail "missing --project-root value"
      [[ -d "$2" ]] || fail "project root does not exist"
      project_root="$(cd "$2" && pwd -P)"
      shift 2
      ;;
    --asset-dir)
      [[ $# -ge 2 ]] || fail "missing --asset-dir value"
      asset_dir="$2"
      shift 2
      ;;
    --help)
      usage
      exit 0
      ;;
    *)
      fail "unknown argument"
      ;;
  esac
done

command -v java >/dev/null 2>&1 || fail "java command is not available"
command -v jar >/dev/null 2>&1 || fail "jar command is not available"

pom_file="${project_root}/pom.xml"
[[ -f "$pom_file" ]] || fail "pom.xml was not found"

pom_value() {
  local tag="$1"
  local value
  value="$(sed -n "s:^[[:space:]]*<${tag}>\\([^<]*\\)</${tag}>[[:space:]]*$:\\1:p" "$pom_file" | head -n 1 | tr -d '\r')"
  [[ -n "$value" ]] || fail "pom.xml is missing <${tag}>"
  printf '%s' "$value"
}

group_id="$(pom_value groupId)"
artifact_id="$(pom_value artifactId)"
version="$(pom_value version)"
jar_filename="${artifact_id}-${version}.jar"

case "$jar_filename" in
  ""|*/*|*\\*)
    fail "candidate jar filename is not filename-only"
    ;;
esac

target_dir="${project_root}/target"
[[ ! -L "$target_dir" ]] || fail "target directory must not be a symlink"
mkdir -p "$target_dir"
[[ ! -L "$target_dir" ]] || fail "target directory must not be a symlink"
target_dir="$(cd "$target_dir" && pwd -P)"
[[ "$target_dir" == "${project_root}/target" ]] || fail "target directory must not be a symlink"

if [[ -z "$asset_dir" ]]; then
  asset_dir="${target_dir}/release-artifact-dry-run"
else
  asset_dir="$(resolve_input_path "$asset_dir")"
fi

asset_parent="$(dirname "$asset_dir")"
asset_name="$(basename "$asset_dir")"
[[ "$asset_dir" != "$target_dir" ]] || fail "asset directory must not be target/"
ensure_under_target "$asset_dir"
case "$asset_name" in
  release-artifact-dry-run|release-artifact-dry-run-*) ;;
  *)
    fail "asset directory name must be release-artifact-dry-run"
    ;;
esac

existing_parent="$asset_parent"
while [[ ! -e "$existing_parent" ]]; do
  [[ "$existing_parent" != "$target_dir" ]] || break
  existing_parent="$(dirname "$existing_parent")"
done

case "$existing_parent" in
  "$target_dir"|"$target_dir"/*) ;;
  *)
    fail "asset directory must be under target/"
    ;;
esac
existing_parent="$(cd "$existing_parent" && pwd -P)"
case "$existing_parent" in
  "$target_dir"|"$target_dir"/*) ;;
  *)
    fail "asset directory must be under target/"
    ;;
esac

mkdir -p "$asset_parent"
asset_parent="$(cd "$asset_parent" && pwd -P)"
asset_dir="${asset_parent}/${asset_name}"
case "$asset_parent" in
  "$target_dir"|"$target_dir"/*) ;;
  *)
    fail "asset directory must be under target/"
    ;;
esac

candidate_jar="${target_dir}/${jar_filename}"
[[ -f "$candidate_jar" ]] || fail "missing candidate jar target/${jar_filename}; run mvn package first"

version_output="$(java -jar "$candidate_jar" --version 2>&1)"
expected_version_output="${artifact_id} ${version}"
[[ "$version_output" == "$expected_version_output" ]] \
  || fail "CLI version output mismatch"

extract_dir="$(mktemp -d "${TMPDIR:-/tmp}/apm-artifact-integrity.XXXXXX")"
cleanup() {
  rm -rf "$extract_dir"
}
trap cleanup EXIT

(
  cd "$extract_dir"
  jar xf "$candidate_jar" \
    META-INF/MANIFEST.MF \
    "META-INF/maven/${group_id}/${artifact_id}/pom.properties"
)

manifest_file="${extract_dir}/META-INF/MANIFEST.MF"
[[ -f "$manifest_file" ]] || fail "jar manifest is missing"
manifest_main_class="$(tr -d '\r' < "$manifest_file" | sed -n 's/^Main-Class: //p' | head -n 1)"
[[ "$manifest_main_class" == "$MAIN_CLASS" ]] || fail "manifest Main-Class mismatch"

metadata_file="${extract_dir}/META-INF/maven/${group_id}/${artifact_id}/pom.properties"
[[ -f "$metadata_file" ]] || fail "Maven artifact metadata is missing"

metadata_value() {
  local key="$1"
  local value
  value="$(tr -d '\r' < "$metadata_file" | sed -n "s/^${key}=//p" | head -n 1)"
  [[ -n "$value" ]] || fail "Maven artifact metadata is missing ${key}"
  printf '%s' "$value"
}

metadata_group_id="$(metadata_value groupId)"
metadata_artifact_id="$(metadata_value artifactId)"
metadata_version="$(metadata_value version)"

[[ "$metadata_group_id" == "$group_id" ]] || fail "Maven artifact metadata groupId mismatch"
[[ "$metadata_artifact_id" == "$artifact_id" ]] || fail "Maven artifact metadata artifactId mismatch"
[[ "$metadata_version" == "$version" ]] || fail "Maven artifact metadata version mismatch"

rm -rf "$asset_dir"
mkdir -p "$asset_dir"
cp "$candidate_jar" "${asset_dir}/${jar_filename}"

if command -v shasum >/dev/null 2>&1; then
  checksum_tool="shasum"
  (
    cd "$asset_dir"
    shasum -a 256 "$jar_filename" > SHA256SUMS
  )
elif command -v sha256sum >/dev/null 2>&1; then
  checksum_tool="sha256sum"
  (
    cd "$asset_dir"
    sha256sum "$jar_filename" > SHA256SUMS
  )
else
  fail "no SHA-256 checksum tool is available"
fi

checksum_file="${asset_dir}/SHA256SUMS"
[[ -f "$checksum_file" ]] || fail "SHA256SUMS was not created"

checksum_line_count="$(wc -l < "$checksum_file" | tr -d '[:space:]')"
[[ "$checksum_line_count" == "1" ]] || fail "SHA256SUMS must contain exactly one line"

checksum_value=""
checksum_name=""
checksum_extra=""
read -r checksum_value checksum_name checksum_extra < "$checksum_file" || fail "SHA256SUMS is unreadable"
[[ -z "$checksum_extra" ]] || fail "SHA256SUMS contains unexpected fields"
[[ "$checksum_value" =~ ^[0-9a-f]{64}$ ]] || fail "SHA256SUMS digest is not SHA-256 lowercase hex"
[[ "$checksum_name" == "$jar_filename" ]] || fail "SHA256SUMS filename does not match candidate jar"
case "$checksum_name" in
  ""|*/*|*\\*|.*)
    fail "SHA256SUMS filename must be filename-only"
    ;;
esac

asset_list="$(find "$asset_dir" -maxdepth 1 -type f -exec basename {} \; | LC_ALL=C sort)"
expected_asset_list="$(printf '%s\nSHA256SUMS\n' "$jar_filename" | LC_ALL=C sort)"
[[ "$asset_list" == "$expected_asset_list" ]] || fail "release asset list mismatch"

(
  cd "$asset_dir"
  if [[ "$checksum_tool" == "shasum" ]]; then
    shasum -a 256 -c SHA256SUMS >/dev/null
  else
    sha256sum -c SHA256SUMS >/dev/null
  fi
)

printf 'Artifact integrity dry-run passed.\n'
printf 'Candidate jar: %s\n' "$jar_filename"
printf 'CLI version: %s\n' "$version_output"
printf 'Manifest Main-Class: %s\n' "$manifest_main_class"
printf 'Maven coordinates: %s:%s:%s\n' "$metadata_group_id" "$metadata_artifact_id" "$metadata_version"
printf 'Release assets: %s, SHA256SUMS\n' "$jar_filename"
printf 'SHA256SUMS: filename-only entry verified.\n'
