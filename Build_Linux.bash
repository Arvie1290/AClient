#!/bin/bash

while true
do
    clear
    echo "================================="
    echo "  Mods Play ./gradlew build"
    echo "================================="
    echo ""
    echo "Good luck bro the debugging :3."
    echo ""

    ./gradlew clean build --info

    echo
    echo "Debugging Done."
    read -n 1 -s -r -p "Press [R] for Restart or [Q] for Quit... " key
    echo

    if [[ "$key" =~ [Qq] ]]; then
        break
    fi
done

echo "Bye bro."
