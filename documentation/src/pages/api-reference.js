import React, {useEffect, useRef} from 'react';
import Layout from '@theme/Layout';
import BrowserOnly from '@docusaurus/BrowserOnly';
import useBaseUrl from '@docusaurus/useBaseUrl';

function SwaggerUILoader() {
  const containerRef = useRef(null);
  const specUrl = useBaseUrl('/openapi.json');

  useEffect(() => {
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = 'https://unpkg.com/swagger-ui-dist@5/swagger-ui.css';
    document.head.appendChild(link);

    const script = document.createElement('script');
    script.src = 'https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js';
    script.onload = () => {
      if (containerRef.current && window.SwaggerUIBundle) {
        window.SwaggerUIBundle({
          url: specUrl,
          domNode: containerRef.current,
          deepLinking: true,
          docExpansion: 'list',
          defaultModelsExpandDepth: -1,
          filter: true,
          tryItOutEnabled: false,
        });
      }
    };
    document.body.appendChild(script);

    return () => {
      document.head.removeChild(link);
      document.body.removeChild(script);
    };
  }, [specUrl]);

  return <div ref={containerRef} />;
}

export default function ApiReference() {
  return (
    <Layout
      title="API Reference"
      description="Otoroshi Biscuit Studio REST API Reference - OpenAPI / Swagger documentation">
      <div style={{padding: '1rem 2rem', maxWidth: 1400, margin: '0 auto'}}>
        <h1>Otoroshi Biscuit Studio REST API</h1>
        <p>
          Otoroshi Biscuit Studio provides a fully featured REST admin API to manage all Biscuit token entities.
          You can also access the OpenAPI spec directly from any running Otoroshi instance with the Biscuit Studio extension installed.
        </p>
        <BrowserOnly fallback={<div style={{padding: '2rem', textAlign: 'center'}}>Loading API Reference...</div>}>
          {() => <SwaggerUILoader />}
        </BrowserOnly>
      </div>
    </Layout>
  );
}
