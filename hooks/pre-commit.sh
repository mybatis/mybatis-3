#!/usr/bin/env bash
#
#    Copyright 2009-2025 the original author or authors.
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#

set -eo pipefail

function format_and_sort() {
  # Run the validate and import check commands, suppressing all output and errors
  if ! mvn formatter:validate impsort:check > /dev/null 2>&1; then
      # If validation failed, fix it by ensuring code is formatted correctly
      # and that imports are sorted as some IDEs automatically change it on save
      mvn formatter:format impsort:sort

      # Fail the commit, so the author can re-select what to commit
      echo "Formatting and/or import sorting were required. Please check and make another commit."
      exit 1
  fi

  # If no error occurred, print a success message
  echo "All files are properly formatted and imports are sorted."
}

format_and_sort