from sklearn.metrics.pairwise import cosine_similarity
from model import model


def match_percentage(text1: str, text2: str) -> float:
    embeddings = model.encode([text1, text2], normalize_embeddings=True)
    score = float(cosine_similarity([embeddings[0]], [embeddings[1]])[0][0])
    # Convert [-1, 1] to [0, 100] and clamp for a user-facing percentage.
    percentage = max(0.0, min(100.0, score * 100.0))
    return round(percentage, 2)
