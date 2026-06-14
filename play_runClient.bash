#!/bin/bash

while true
do
    clear
    echo "================================="
    echo "AClient Play ./gradlew runClient"
    echo "================================="
    echo ""
    echo "Good luck bro the debugging :3."
    echo ""

    ./gradlew runClient

    echo
    echo "Game Selesai."
    read -n 1 -s -r -p "Tekan [R] untuk Restart atau [Q] untuk Keluar... " key
    echo

    if [[ "$key" =~ [Qq] ]]; then
        break
    fi
done

echo "Bye bro."
