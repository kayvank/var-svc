 docker run -d --name dd-agent \
   -v /var/run/docker.sock:/var/run/docker.sock:ro \
   -v /proc/:/host/proc/:ro \
   -v /sys/fs/cgroup/:/host/sys/fs/cgroup:ro \
   -e API_KEY=${DATADOG_API_KEY} \
   datadog/docker-dd-agent:latest
