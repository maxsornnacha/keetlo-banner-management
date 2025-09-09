<script lang="ts">
import { onMounted, watch } from "vue";
import { useRoute } from "vue-router";

export default {
  name: "DocumentationPage",
  setup() {
    const route = useRoute();
    const BaseAPIUrl = process.env.VUE_APP_API_URL;
    const BaseUrl = window.location.origin;

    const scrollToHash = () => {
      const hash = route.hash;
      if (hash) {
        const element = document.getElementById(hash.replace("#", ""));
        if (element) {
          element.scrollIntoView({ behavior: "smooth" });
        }
      } else {
        window.scrollTo(0, 0);
      }
    };

    onMounted(() => {
      scrollToHash();
    });

    watch(route, () => {
      scrollToHash();
    });

    return { BaseUrl, BaseAPIUrl };
  },
};
</script>
<template>
  <div class="py-4">
    <!-- Page Header -->
    <header class="mb-8 text-center">
      <h1 class="text-3xl font-bold">Banner Widget Documentation</h1>
      <p class="text-lg text-gray-500">
        Learn how to integrate the banner widget into your website using script,
        iframe, and API.
      </p>
    </header>

    <!-- Embed Script Section -->
    <section class="mb-8">
      <h2 id="script" class="text-2xl font-semibold text-indigo-500 mb-4">
        Embed Script
      </h2>
      <p class="text-gray-600">
        To display a banner on your site, you can use the following script. This
        script will dynamically load banners from your campaign:
      </p>
      <pre class="bg-gray-100 p-4 rounded-lg">
        <code>
          &lt;div id="keetlo-banner" data-campaign="CAMPAIGN_ID" data-width="1900" data-height="420"&gt;&lt;/div&gt;
          &lt;script&gt;
            (function(w,d,s,u,i){
              var js=d.createElement(s), f=d.getElementsByTagName(s)[0];
              js.async=1; js.src=u; js.dataset.key=i; f.parentNode.insertBefore(js,f);
            })(window,document,'script','{{BaseUrl}}/widgets/banner.min.js','PUBLIC_API_KEY');
          &lt;/script&gt;
        </code>
      </pre>
      <p class="text-gray-600 mt-4">
        Replace <strong>CAMPAIGN_ID</strong> with the ID of your campaign, and
        <strong>PUBLIC_API_KEY</strong> with your unique API key.
      </p>
    </section>

    <!-- Iframe Embed Section -->
    <section class="mb-8">
      <h2 id="iframe-embed" class="text-2xl font-semibold text-indigo-500 mb-4">
        Iframe Embed
      </h2>
      <p class="text-gray-600">
        Alternatively, you can use the following iframe code to embed the banner
        directly on your website. This method keeps the widget isolated from the
        rest of your page styles:
      </p>
      <pre class="bg-gray-100 p-4 rounded-lg">
        <code>
          &lt;iframe 
            src="{{BaseUrl}}/widgets/banner-embed.html?campaign=CAMPAIGN_ID&key=PUBLIC_API_KEY" 
            style="width: 100%; max-width:1900px; min-height:420px; border: none;" 
            loading="lazy"&gt;&lt;/iframe&gt;
        </code>
      </pre>
      <p class="text-gray-600 mt-4">
        Replace <strong>CAMPAIGN_ID</strong> and
        <strong>PUBLIC_API_KEY</strong> as needed. This will load the banner in
        a self-contained iframe on your page.
      </p>
    </section>

    <!-- API Usage Section -->
    <section class="mb-8">
      <h2 id="api-usage" class="text-2xl font-semibold text-indigo-500 mb-4">
        API Usage
      </h2>
      <p class="text-gray-600">
        To programmatically interact with the system, you can use the following
        API endpoint to fetch banners for a campaign:
      </p>
      <pre class="bg-gray-100 p-4 rounded-lg">
        <code>
          GET {{BaseAPIUrl}}/public/campaigns/{campaignId}/banners?key=PUBLIC_API_KEY
        </code>
      </pre>
      <p class="text-gray-600 mt-4">
        This API returns a list of banners for a specific campaign. Replace
        <strong>campaignId</strong> with your campaign ID, and
        <strong>PUBLIC_API_KEY</strong> with your API key.
      </p>
      <p class="text-gray-600 mt-4"><strong>Example Request:</strong></p>
      <pre class="bg-gray-100 p-4 rounded-lg">
        <code>
          GET {{BaseAPIUrl}}/public/campaigns/1cf1daec-2b19-4895-99e3-7d255ea432b1/banners?key=PUBLIC_API_KEY
        </code>
      </pre>
      <p class="text-gray-600 mt-4"><strong>Example Response:</strong></p>
      <pre class="bg-gray-100 p-4 rounded-lg">
        <code>
          {
            "status": "success",
            "data": {
              "banners": [
                {
                  "bannerId": "abc123",
                  "bannerName": "Example Banner"
                  "imageUrl": "{{BaseAPIUrl}}/images/banners/banner1.jpg",
                  "imageWidth": 1900,
                  "imageHeight": 430,
                  "link": "https://target-website.com"
                }
              ]
            }
          }
        </code>
      </pre>
      
    </section>

    <!-- Contact Support Section -->
    <section>
      <h2 class="text-2xl font-semibold text-indigo-500 mb-4">Need Help?</h2>
      <p class="text-gray-600">
        If you encounter any issues or have questions about integrating the
        banner widget, please
        <a href="mailto:sornnacha.bu@gmail.com" class="text-indigo-600"
          >contact support</a
        >.
      </p>
    </section>
  </div>
</template>
<style scoped>
pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}

code {
  font-size: 0.875rem;
  background-color: #f1f5f9;
  padding: 8px;
  border-radius: 4px;
  display: block;
}
</style>
