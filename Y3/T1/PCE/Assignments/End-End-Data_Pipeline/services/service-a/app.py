from flask import Flask, request, jsonify
import logging, json, time, random, os
app = Flask(__name__)
logdir = "/app/logs"
os.makedirs(logdir, exist_ok=True)
handler = logging.FileHandler(f"{logdir}/service-a.log")
handler.setFormatter(logging.Formatter('%(message)s'))
logger = logging.getLogger('service-a')
logger.addHandler(handler)
logger.setLevel(logging.INFO)

@app.route('/api/do', methods=['GET','POST'])
def do():
    start=time.time()
    time.sleep(random.uniform(0.02,0.1))
    latency = int((time.time()-start)*1000)
    entry = {"ts": time.time(), "service":"service-a", "status":200, "latency_ms":latency}
    logger.info(json.dumps(entry))
    return jsonify(entry)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
