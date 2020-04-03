#!/usr/bin/env python3

from waitress import serve
from maid_api import app
from maid import Maid

import coloredlogs
import config
import logging
import threading


if __name__ == '__main__':
    api_server_thread = threading.Thread(
        target=serve, args=(app,), kwargs={
            'host': config.API_SERVER_HOST,
            'port': config.API_SERVER_PORT
        }, daemon=True)
    api_server_thread.start()

    maid_ = Maid()
    maid_.run()

    api_server_thread.join()
