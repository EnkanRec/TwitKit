from flask import Flask, request, jsonify, make_response, abort
from flask_restx import Resource, Api
from oven import tweet_page_bp
from api import oven_api
from requests import post, options

import logging
import sys
import json
import config
import coloredlogs

app = Flask(__name__, static_url_path='/static')
api = Api(app)

if config.APP_LOG_FILE:
    app_log_handler = logging.FileHandler(config.APP_LOG_FILE)
    app.logger.addHandler(app_log_handler)

coloredlogs.install(
    level=logging.DEBUG if config.LOG_DEBUG else logging.INFO)

app.register_blueprint(tweet_page_bp, url_prefix='/internal')
api.add_namespace(oven_api, path='/api/oven')


@app.route('/api_proxy/<module>/<path:path>', methods=['POST', 'OPTIONS'])
def proxy(module, path):
    if module == 'maid':
        url = f'{config.MAID_API_BASE}/{path}'
    elif module == 'fridge':
        url = f'{config.FRIDGE_API_BASE}/{path}'
    else:
        abort(404)

    if request.method == 'POST':
        post_data = request.json
        resp = post(url, json=post_data)
        response = jsonify(resp.json())
    else:
        resp = options(url)
        response = make_response()

    return response, resp.status_code


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
