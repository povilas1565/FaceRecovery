import sys
import time
import cv2
import face_recognition
import numpy as np
from pathlib import Path

def load_image(path):
    image = cv2.imread(str(path))
    return cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

def simulate_gan_inpainting(image):
    face_locations = face_recognition.face_locations(image)
    if not face_locations:
        return image

    top, right, bottom, left = face_locations[0]

    mask = np.zeros(image.shape[:2], dtype=np.uint8)
    eye_y = top + (bottom - top) // 3
    mask[eye_y:eye_y+20, left:right] = 255

    inpainted = cv2.inpaint(image, mask, 3, cv2.INPAINT_TELEA)
    return inpainted

def get_face_embedding(image):
    encodings = face_recognition.face_encodings(image)
    return encodings[0] if encodings else None

def cosine_similarity(a, b):
    return np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("0.0;0.0")
        sys.exit(1)

    path = Path(sys.argv[1])
    start = time.time()

    image_with_glasses = load_image(path)
    restored = simulate_gan_inpainting(image_with_glasses)

    embedding_restored = get_face_embedding(restored)
    embedding_original = get_face_embedding(image_with_glasses)

    if embedding_original is None or embedding_restored is None:
        print("0.0;0.0")
    else:
        score = cosine_similarity(embedding_restored, embedding_original)
        duration = round(time.time() - start, 3)
        print(f"{round(score, 4)};{duration}")