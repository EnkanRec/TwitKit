import unittest
import tid_code
import tempfile
import os

from PIL import Image


class TestTidCode(unittest.TestCase):

    def test_checksum(self):
        self.assertEqual(tid_code.calculate_checksum(0x1234), 0xF1)
        self.assertEqual(tid_code.calculate_checksum(0x556832), 0x27)

    def test_encode(self):
        self.assertEqual(
            tid_code.encode(0x1234, key=0),
            [
                True, False, True, False, True, False, True, False,  # 1111 F
                False, True, False, True, False, True, True, False,  # 0001 1
                False, True, False, True, False, True, False, True,  # 0000 0
                False, True, False, True, False, True, False, True,  # 0000 0
                False, True, False, True, False, True, True, False,  # 0001 1
                False, True, False, True, True, False, False, True,  # 0010 2
                False, True, False, True, True, False, True, False,  # 0011 3
                False, True, True, False, False, True, False, True,  # 0100 4
            ])

    def test_decode(self):
        self.assertEqual(tid_code.decode(tid_code.encode(0x5678)), 0x5678)
        self.assertEqual(tid_code.decode(tid_code.encode(0x2333)), 0x2333)
        self.assertEqual(tid_code.decode(tid_code.encode(0xffaa45)), 0xffaa45)

    def test_integration(self):
        self.assertEqual(
            tid_code.decode_from_image(tid_code.generate_code_image(20, 20,
                                                                    2233)),
            2233)

    def test_image_integration(self):
        im = Image.new('RGBA', (1000, 1000))
        test_tid = 1000
        tid_code.add_code_to_image(im, test_tid, 0, 0, 20, 20)
        tid = tid_code.read_code_from_image(im, 0, 0, 20, 20)
        self.assertEqual(test_tid, tid)


if __name__ == '__main__':
    unittest.main()
