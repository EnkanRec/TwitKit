from flask import Flask, request
from flask_restplus import Resource, Api
from oven import tweet_page_bp
from api import oven_api

import logging
import sys
import json
import config

app = Flask(__name__, static_url_path='/static')
api = Api(app)

if config.APP_LOG_FILE:
    app_log_handler = logging.FileHandler(config.APP_LOG_FILE)
    app.logger.addHandler(app_log_handler)

app.logger.setLevel(logging.getLevelName(config.LOG_LEVEL))

app.register_blueprint(tweet_page_bp, url_prefix='/internal')
api.add_namespace(oven_api, path='/api/oven')


@app.after_request
def after_request(response):
    if request.path.startswith('/api/oven'):
        try:
            response_data = json.loads(response.get_data())
        except:
            return response
        if 'code' not in response_data:
            response_data['code'] = response.status_code
            status_code = 200
            response.set_data(json.dumps(response_data))
    return response


if __name__ == '__main__':
    app.run(debug=True)
