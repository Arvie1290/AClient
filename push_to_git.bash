#!/bin/bash

echo "================================="
echo "      AClient Git Updater"
echo "================================="
echo ""
echo "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"

read -p "Commit message: " msg

git add .

git commit -m "$msg"

git push

echo
echo "Upload selesai."
read -n 1 -s -r -p "Tekan tombol apa saja untuk keluar..."
echo
