import pickle
import pandas as pd
import random
from flask import Flask, request, jsonify

app = Flask(__name__)

# ===============================
# LOAD MODEL
# ===============================
rules = pickle.load(open("rules.pkl", "rb"))
df_encoded = pickle.load(open("df_encoded.pkl", "rb"))

print("Model loaded successfully")

# ===============================
# RECOMMENDATION FUNCTION
# ===============================
def recommend(cart_products, min_lift=0.8, top_n=10):

    cart_products = set(cart_products)

    # 1. Find matching rules
    filtered = rules[
        rules["antecedents"].apply(
            lambda x: len(set(x).intersection(cart_products)) > 0
        )
    ]

    # 2. If rules exist → use them
    if not filtered.empty:

        filtered = filtered[
            (filtered["lift"] >= min_lift) &
            (filtered["confidence"] >= 0.05)
        ]

        if not filtered.empty:

            recommendations = (
                filtered.explode("consequents")
                .groupby("consequents")
                .agg({"confidence": "max", "lift": "max"})
                .reset_index()
            )

            recommendations["score"] = (
                recommendations["confidence"] * recommendations["lift"]
            )

            recommendations = recommendations.sort_values(
                "score",
                ascending=False
            )

            return recommendations.head(top_n)["consequents"].tolist()

    # 3. SMART fallback (still product names)
    popular = (
        df_encoded.sum()
        .sort_values(ascending=False)
        .head(top_n * 3)
        .index.tolist()
    )

    random.shuffle(popular)

    return popular[:top_n]


# ===============================
# API ENDPOINT
# ===============================
@app.route("/recommend", methods=["POST"])
def get_recommendations():

    data = request.get_json()

    # INPUT: ["tente"] OR ["sac de couchage"]
    cart = data.get("cart", [])

    # ML RESULT (product names)
    result = recommend(cart)

    return jsonify({
        "recommendations": result
    })


# ===============================
# RUN SERVER
# ===============================
if __name__ == "__main__":
    app.run(port=5000, debug=True)