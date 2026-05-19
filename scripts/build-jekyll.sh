#!/usr/bin/env bash
set -euo pipefail

# Jekyll ビルドスクリプト
# GitHub Pages 用の Markdown を HTML に変換する

SOURCE_DIR="${1:-build/pages}"
DEST_DIR="${2:-../pages_converted}"

echo "=== Jekyll ビルド開始 ==="
echo "ソースディレクトリ: ${SOURCE_DIR}"
echo "出力ディレクトリ: ${DEST_DIR}"

if [[ ! -d "${SOURCE_DIR}" ]]; then
  echo "エラー: ソースディレクトリが存在しません: ${SOURCE_DIR}" >&2
  exit 1
fi

cd "${SOURCE_DIR}"

export JEKYLL_ENV="${JEKYLL_ENV:-production}"
export PAGES_REPO_NWO="${PAGES_REPO_NWO:-}"

echo "JEKYLL_ENV: ${JEKYLL_ENV}"
echo "PAGES_REPO_NWO: ${PAGES_REPO_NWO}"

bundle exec github-pages build \
  --source . \
  --destination "${DEST_DIR}" \
  --verbose

echo "=== Jekyll ビルド完了 ==="
