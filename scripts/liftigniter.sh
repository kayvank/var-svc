curl -H "Content-Type: application/json" -d '{
  "apiKey":       "b96f93c2-0093-427e-9876-e90338d351b3",
  "currentItem":  "USSD21600648",
  "maxCount":     5,
  "showCount":     5,
"userId": null,
"anonId": "KKazeminejad02",
  "requestFields": ["id"],
  "country":      "US"
}' -X POST    $LIFTIGNITER_URL

read var1

curl -H "Content-Type: application/json" -d '{
  "apiKey":       "b96f93c2-0093-427e-9876-e90338d351b3",
  "currentItem":  "USSD21600369",
  "maxCount":     5,
  "showCount":     5,
"userId": "KKazeminejad02",
  "requestFields": ["id"],
  "country":      "US"
}' -X POST $LIFTIGNITER_URL
