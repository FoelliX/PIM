#!/bin/bash

cd target/build
mkdir answers

zip -u $1 tool.properties
rm tool.properties