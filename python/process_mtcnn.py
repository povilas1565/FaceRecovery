import sys
import time
from pathlib import Path
import numpy as np
from PIL import Image

import torch
from facenet_pytorch import MTCNN, InceptionResnetV1

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

mtcnn = MTCNN(image_size=160, margin=0, device=device)
resnet = InceptionResnetV1(pretrained='vggface2').eval().to(device)

def load_image(path):
    return Image.open(path).convert('RGB')

def get_embedding(image):
    face = mtcnn(image)
    if face is None:
        return None
    face = face.unsqueeze(0).to(device)
    with torch.no_grad():
        emb = resnet(face)
    return emb.cpu().numpy().flatten()

def cosine_similarity(a, b):
    return np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("0.0;0.0")
        sys.exit(1)

    path = Path(sys.argv[1])
    start = time.time()

    image = load_image(path)

    emb1 = get_embedding(image)
    emb2 = get_embedding(image)  # сравнение одного и того же изображения (или можно загрузить "чистое")

    if emb1 is None or emb2 is None:
        print("0.0;0.0")
    else:
        score = cosine_similarity(emb1, emb2)
        duration = round(time.time() - start, 3)
        print(f"{round(score, 4)};{duration}")