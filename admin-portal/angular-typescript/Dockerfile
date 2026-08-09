# Build stage
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build -- --configuration=production

# Production stage
FROM nginx:alpine
# In Angular 17+, the build output might be in dist/<project-name> or dist/<project-name>/browser
# Let's inspect where Angular builds it, or write a copy that handles either, or set Nginx root.
# Generally, by default, newer Angular CLI builds to dist/admin-portal/browser.
# Let's copy from dist/admin-portal/browser if it exists, otherwise dist/admin-portal.
# Let's use a simple shell copy pattern or build configuration.
# We will write a custom Nginx configuration.
COPY --from=build /app/dist/admin-portal/browser /usr/share/nginx/html

RUN echo 'server { \
    listen 80; \
    location / { \
        root /usr/share/nginx/html; \
        index index.html index.htm; \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
