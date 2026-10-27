from flask import Flask, jsonify, request
from flask_cors import CORS
from matcher import match_percentage

app = Flask(__name__)
CORS(app)


@app.get("/health")
def health():
    return jsonify({"status": "ok", "service": "deep-learning-matchmaker"})


@app.post("/ai/match")
def ai_match():
    data = request.get_json(silent=True) or {}
    student1 = str(data.get("student1", "")).strip()
    student2 = str(data.get("student2", "")).strip()

    if not student1 or not student2:
        return jsonify({"error": "student1 and student2 are required"}), 400

    score = match_percentage(student1, student2)
    if score >= 80:
        recommendation = "Highly Recommended"
    elif score >= 60:
        recommendation = "Good Match"
    elif score >= 40:
        recommendation = "Possible Match"
    else:
        recommendation = "Low Match"

    return jsonify({
        "match_percentage": score,
        "recommendation": recommendation
    })


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=True)
