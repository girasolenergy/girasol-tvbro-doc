#!/usr/bin/env bash
set -euo pipefail

# Jekyll ビルドスクリプト
# GitHub Pages 用の Markdown を HTML に変換する

# スクリプトファイルの実際のパスを基準にリポジトリルートを特定
# シンボリックリンク経由で実行された場合も正しく動作する
SCRIPT_PATH="${BASH_SOURCE[0]}"
while [[ -L "${SCRIPT_PATH}" ]]; do
  SCRIPT_DIR="$(cd "$(dirname "${SCRIPT_PATH}")" && pwd)"
  SCRIPT_PATH="$(readlink "${SCRIPT_PATH}")"
  [[ "${SCRIPT_PATH}" != /* ]] && SCRIPT_PATH="${SCRIPT_DIR}/${SCRIPT_PATH}"
done
SCRIPT_DIR="$(cd "$(dirname "${SCRIPT_PATH}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# 固定パス（リポジトリルートからの絶対パス）
SOURCE_DIR="${REPO_ROOT}/build/pages"
DEST_DIR="${REPO_ROOT}/build/pages_converted"

echo "=== Jekyll ビルド開始 ==="
echo "リポジトリルート: ${REPO_ROOT}"
echo "ソースディレクトリ: ${SOURCE_DIR}"
echo "出力ディレクトリ: ${DEST_DIR}"

# ソースディレクトリの存在確認
if [[ ! -d "${SOURCE_DIR}" ]]; then
  echo "エラー: ソースディレクトリが存在しません: ${SOURCE_DIR}" >&2
  exit 1
fi

# 環境変数のバリデーション
if [[ -z "${PAGES_REPO_NWO:-}" ]]; then
  echo "エラー: 環境変数 PAGES_REPO_NWO が設定されていません" >&2
  echo "使用例: PAGES_REPO_NWO='owner/repo' ./scripts/build-jekyll.sh" >&2
  exit 1
fi

cd "${SOURCE_DIR}"

export JEKYLL_ENV="${JEKYLL_ENV:-production}"
export PAGES_REPO_NWO

echo "JEKYLL_ENV: ${JEKYLL_ENV}"
echo "PAGES_REPO_NWO: ${PAGES_REPO_NWO}"

bundle exec github-pages build \
  --source . \
  --destination "${DEST_DIR}" \
  --verbose

echo "=== Jekyll ビルド完了 ==="
