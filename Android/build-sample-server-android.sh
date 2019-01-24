#!/bin/bash
#
# build-sample-server-android.sh (-h for help)
#
# The script builds all the samples which are part of the Android VNC Automotive
# Server SDK distribution.
#
# Copyright (C) 2014-2018 VNC Automotive Ltd.  All Rights Reserved.

set -e
set -E

# The possible components that can be built. Not all of them are necessarily
# available, or needed. The script builds the needed and present components,
# out of all the possible ones.
POSSIBLE_COMPONENTS="mobileServer bearers"
# This is where the binaries get copied to.
BINARIES="$(pwd)/binaries"

ANDROID_TARGET="android-23"

# Gets the path to a certain component. Returns a zero-length string if the
# component is not found.
#
# $1 - The name of the component
function get_component_path
{
  case "$1" in
    bearers)
      echo "Samples/Bearers"
      ;;
    mobileServer)
      echo "Samples/Server"
      ;;
    *)
      echo ""
      ;;
  esac
}

# Gets the list of components available to build.
function get_available_components
{
  local MS_COMP=
  for C in $POSSIBLE_COMPONENTS
  do
    local P="$(get_component_path "$C")"
    if [ -n "$P" -a -d "$P" ]
    then
      MS_COMP="$MS_COMP $C"
    fi
  done
  echo $MS_COMP
}

# Output an error message and exit.
function die
{
  echo -e "$1" >&2
  echo -e "Samples build failed"
  exit 1
}

function build_with_gradle
{
  if [ -n "$DEBUG" ]
  then
    gradle assembleDebug
  else
    gradle assembleRelease
  fi

}

# Builds the Server sample application.
#
# The current dir is the component dir.
function build_mobile_server
{
  local apk_name="AndroidSampleServer"

  if [ -n "$KEY_STORE" ]
  then
    local keystore_from_root="$KEY_STORE"
    if [ "${KEY_STORE:0:1}" != "/" ]
    then
      keystore_from_root="../../$KEY_STORE"
    fi
    echo -n > app/keystore.properties
    echo "storeFile=$keystore_from_root" >> app/keystore.properties
    echo "storePassword=$PASSWD" >> app/keystore.properties
    echo "keyAlias=$ALIAS_NAME" >> app/keystore.properties
    echo "keyPassword=$ALIAS_PASSWD" >> app/keystore.properties
  fi

  build_with_gradle

  if [ -n "$DEBUG" ]
  then
      local build_type="debug"
  else
      local build_type="release"
  fi

  cp -f app/build/outputs/apk/$build_type/app-$build_type*.apk "$BINARIES"/$apk_name-$build_type.apk
}

function build_bearers
{
  build_with_gradle
}

AVAILABLE_COMPONENTS="$(get_available_components)"

USAGE="$(basename "$0") [-g] [-h] [-K KEYSTORE] [-P PASSWD] COMPONENTS...
  -g                - build a debug build.
  -h                - show this help message and exit.
  -K KEYSTORE       - specify the keystore to use to sign the apks.
  -P PASSWD         - specify the password used for the keystore.
  -N ALIAS_NAME     - specify the alias name for the key stored in the keystore.
  -W ALIAS_PASSWD   - specify the password for the desired key
                      (this is required when it differs from PASSWD).

  By default it builds all the available sample components, but if you wish to
  build a subset of the components, then enumerate them at the end of the
  script.

  Possible components: $AVAILABLE_COMPONENTS.

  All the binaries are copied to the binaries directory, under the
  architecture-specific sub-directory."

DEBUG=
KEY_STORE=
PASSWD=
ALIAS_NAME=
ALIAS_PASSWD=

while getopts ghK:P:N:W: OPTION
do
  case "$OPTION" in
    g)
      DEBUG=y
      ;;
    h)
      echo -e "$USAGE"
      exit
      ;;
    K)
      KEY_STORE="$OPTARG"
      ;;
    P)
      PASSWD="$OPTARG"
      ;;
    N)
      ALIAS_NAME="$OPTARG"
      ;;
    W )
      ALIAS_PASSWD="$OPTARG"
      ;;
    *)
      die "$USAGE"
      ;;
  esac
done

COMPONENTS=${@:OPTIND}

if [ -z "$COMPONENTS" ]
then
  COMPONENTS="$AVAILABLE_COMPONENTS"
fi

if [ -z "$ALIAS_NAME" ]
then
  ALIAS_NAME="android"
fi

if [ -z "$ALIAS_PASSWD" ]
then
  ALIAS_PASSWD="$PASSWD"
fi

echo "Building '$COMPONENTS' to '$BINARIES'"

mkdir -p "$BINARIES"
for COMP in $COMPONENTS
do
  P="$(get_component_path "$COMP")"
  if [ -z "$P" -o ! -d "$P" ]
  then
    echo -e "Warning: unable to build '$COMP', not available" >&2
    continue
  fi

  pushd "$P"
  echo "Building '$COMP'"
  case "$COMP" in
    "bearers") build_bearers ;;
    "mobileServer") build_mobile_server ;;
  esac
  popd
done

echo "Samples build succeeded"
