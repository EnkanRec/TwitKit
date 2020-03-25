#!/usr/bin/env python3

from waitress import serve
from app import app

import coloredlogs
import config
import logging
import threading


if __name__ == '__main__':
    coloredlogs.install(
        level=logging.DEBUG if config.LOG_DEBUG else logging.INFO)

    serve(app, host=config.API_SERVER_HOST, port=config.API_SERVER_PORT)
