#!/usr/bin/env python3

from waitress import serve
from app import app

import coloredlogs
import config
import logging
import threading


if __name__ == '__main__':
    serve(app, host=config.API_SERVER_HOST, port=config.API_SERVER_PORT)
