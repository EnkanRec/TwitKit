from PIL import Image
from typing import List
from crc8 import crc8

import math
import requests
import config


TID_CODE_KEY = config.TID_CODE_KEY & 0xff


def calculate_checksum(tid: int) -> int:
    bytes_ = []
    for i in range(3):
        bytes_.append((tid >> (16 - 8 * i)) & 0xff)
    bytes_ = bytes(bytes_)
    return int(crc8(bytes_).digest()[0])


def encode(tid: int, key=TID_CODE_KEY) -> List[bool]:
    '''
    返回bool列表，表示编码后的bits。
    参数：tid -- 支持最大24位
    '''
    checksum = calculate_checksum(tid) ^ key
    data = tid & 0xffffff | (checksum << 24)
    encoded = []
    for i in range(32):
        bit = (data >> (31 - i)) & 1
        if bit:
            encoded += [True, False]
        else:
            encoded += [False, True]
    return encoded


def decode(encoded_tid: List[bool], key=TID_CODE_KEY) -> int:
    '''
    返回解码后的tid。
    参数：表示bits的bool列表。
    如果输入不合法，抛出ValueError异常。
    '''
    if len(encoded_tid) != 64:
        raise ValueError('输入长度不符合要求')
    decoded_with_checksum = 0
    for i in range(0, 64, 2):
        if encoded_tid[i] == True and encoded_tid[i+1] == False:
            decoded_with_checksum |= 1 << (31 - i // 2)
        elif encoded_tid[i] == False and encoded_tid[i+1] == True:
            continue
        else:
            raise ValueError('曼彻斯特编码有误')
    tid = decoded_with_checksum & 0xffffff
    checksum = (decoded_with_checksum >> 24) ^ key
    if calculate_checksum(tid) != checksum:
        raise ValueError('校验码有误')
    return tid


def code_image_properties_check(n_levels, total_bits):
    if int(math.log2(n_levels)) != math.log2(n_levels):
        raise Exception('levels长度必须是2的整数次方')
    if total_bits < 64:
        raise Exception("指定的参数容量不足")


def generate_code_image(width, height, tid, rows=5, cols=5,
                        levels=(0x7f, 0xff)) -> Image:
    n_level_bits = int(math.log2(len(levels)))
    total_bits = rows * cols * n_level_bits * 3

    code_image_properties_check(n_level_bits, total_bits)

    pad_bits = total_bits - 64
    encoded_bits = encode(tid)
    for i in range(pad_bits):
        encoded_bits.append(bool(i & 1))
    levels_matrix = [[[0, 0, 0] for _ in range(cols)] for _ in range(rows)]
    for i in range(rows * cols):
        for j in range(n_level_bits):
            for k in range(3):
                if encoded_bits[i + j * total_bits // (3 * n_level_bits) +
                                k * total_bits // 3]:
                    levels_matrix[i // cols][i % cols][k] |= 1 << j

    scaled_width = width * cols // math.gcd(width, cols)
    scaled_height = height * rows // math.gcd(height, rows)
    im = Image.new('RGB', (scaled_width, scaled_height))
    for i in range(scaled_width):
        for j in range(scaled_height):
            c = int(i / (scaled_width / cols))
            r = int(j / (scaled_height / rows))
            rgb = [levels[value] for value in levels_matrix[r][c]]
            im.putpixel((i, j), tuple(rgb))
    im.getpixel
    im.thumbnail((width, height), resample=Image.BICUBIC)
    return im


def decode_from_image(im: Image, rows=5, cols=5,
                      levels=(0x7f, 0xff)):
    '''返回：tid'''

    im.thumbnail((rows, cols), Image.BICUBIC)

    im_w, im_h = im.size

    n_level_bits = int(math.log2(len(levels)))
    total_bits = rows * cols * n_level_bits * 3
    unpacked_bits = [None] * total_bits

    code_image_properties_check(n_level_bits, total_bits)

    for i in range(cols):
        for j in range(rows):
            block_center = (i, j)
            px = im.getpixel(block_center)
            for k in range(3):
                level = min(levels, key=lambda x: abs(x - px[k]))
                bits = levels.index(level)
                for l in range(n_level_bits):
                    bit = (bits >> l) & 1
                    unpacked_bits[i + j * cols +
                                  l * total_bits // (3 * n_level_bits) +
                                  k * total_bits // 3] = bool(bit)
    unpacked_bits = unpacked_bits[:64]
    return decode(unpacked_bits)


def get_scaled_area(x, y, w, h, orig_size):
    scale = orig_size[0] / config.VIEWPORT_WIDTH
    x *= scale
    y *= scale
    w *= scale
    h *= scale

    if x < 0:
        x = orig_size[0] + x - w
    if y < 0:
        y = orig_size[1] + y - h

    return int(x), int(y), int(w + x), int(h + y)


def add_code_to_image(image, tid, x, y, w, h):
    '''给image打上二维码（in-place）'''
    actual_code_area = get_scaled_area(x, y, w, h, image.size)
    x0, y0, x1, y1 = actual_code_area
    code = generate_code_image(x1 - x0, y1 - y0, tid)
    image.paste(code, actual_code_area)


def read_code_from_image(image, x, y, w, h):
    code = image.crop(get_scaled_area(x, y, w, h, image.size))
    return decode_from_image(code)


def read_code_from_image_url(url, x, y, w, h):
    im = Image.open(requests.get(url, stream=True).raw)
    return read_code_from_image(im, x, y, w, h)
