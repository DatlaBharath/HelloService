name: Migrate from Jenkins

on:
  push:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v2

      - name: Run Shell Command
        run: |
          ls
          echo "this one is executed easily"

      - name: Post Success
        if: success()
        run: echo "Build succeeded"

      - name: Post Failure
        if: failure()
        run: echo "Build failed"