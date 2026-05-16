from flask import Flask, request, jsonify
from flask_cors import CORS
import pickle
import requests
import pandas as pd
from datetime import date

app = Flask(__name__)
CORS(app)

model = pickle.load(open('model.pkl', 'rb'))

def get_meteo(date_arrivee):
    try:
        response = requests.get(
            "https://archive-api.open-meteo.com/v1/archive",
            params={
                "latitude":   36.4561,
                "longitude":  10.7376,
                "start_date": str(date_arrivee),
                "end_date":   str(date_arrivee),
                "daily":      "temperature_2m_mean",
                "timezone":   "Africa/Tunis"
            }
        )
        data = response.json()
        temp = data['daily']['temperature_2m_mean'][0]
        if temp > 30:   return 2
        elif temp < 15: return 0
        else:           return 1
    except:
        return 1  # Normal par defaut

@app.route('/predict', methods=['POST'])
def predict():
    data = request.json

    date_arrivee = date.fromisoformat(data['date_arrivee'])
    lead_time    = (date_arrivee - date.today()).days
    date_depart  = date.fromisoformat(data['date_depart'])
    duree_sejour = (date_depart - date_arrivee).days
    deposit_type = 1 if data['paiement'] == 'Carte' else 0
    meteo        = get_meteo(date_arrivee)

    features = pd.DataFrame([{
        'lead_time':         lead_time,
        'deposit_type':      deposit_type,
        'is_repeated_guest': data['is_repeated_guest'],
        'duree_sejour':      duree_sejour,
        'nb_personnes':      data['nb_personnes'],
        'meteo':             meteo
    }])

    proba      = model.predict_proba(features)[0][1]
    prediction = proba > 0.5

    return jsonify({
        'annulation':   bool(prediction),
        'score_risque': round(proba * 100, 1),
        'message':      'Annulation probable' if prediction else 'Reservation stable'
    })

if __name__ == '__main__':
    app.run(debug=True, port=5000)