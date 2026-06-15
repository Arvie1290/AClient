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
    echo "Debugging Done."
    read -n 1 -s -r -p "Press [R] for Restart or [Q] for Quit... " key
    echo

    if [[ "$key" =~ [Qq] ]]; then
        break
    fi
done

echo "Bye bro."
