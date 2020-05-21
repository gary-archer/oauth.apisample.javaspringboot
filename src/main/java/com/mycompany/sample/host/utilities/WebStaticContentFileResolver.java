package com.mycompany.sample.host.utilities;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;

/*
 * Resolve requests for web static content files
 */
public final class WebStaticContentFileResolver extends PathResourceResolver {

    private String spaRootLocation;
    private String loopbackRootLocation;
    private String desktopRootLocation;
    private String androidRootLocation;
    private String iosRootLocation;

    public WebStaticContentFileResolver(
            final String spaRootLocation,
            final String loopbackRootLocation,
            final String desktopRootLocation,
            final String androidRootLocation,
            final String iosRootLocation) {

        this.spaRootLocation = spaRootLocation;
        this.loopbackRootLocation = loopbackRootLocation;
        this.desktopRootLocation = desktopRootLocation;
        this.androidRootLocation = androidRootLocation;
        this.iosRootLocation = iosRootLocation;
    }

    /*
     * The entry point deals with both web and desktop static content
     */
    @Override
    protected Resource getResource(final String resourcePath, final Resource location) {

        if (resourcePath.toLowerCase().startsWith("spa")) {
            return this.getSinglePageAppResource(resourcePath, location);
        }

        if (resourcePath.toLowerCase().startsWith("loopback")) {
            return this.getLoopbackResource(resourcePath, location);
        }

        if (resourcePath.toLowerCase().startsWith("desktop")) {
            return this.getDesktopResource(resourcePath, location);
        }

        if (resourcePath.toLowerCase().startsWith("android")) {
            return this.getAndroidResource(resourcePath, location);
        }

        if (resourcePath.toLowerCase().startsWith("ios")) {
            return this.getIosResource(resourcePath, location);
        }

        if (resourcePath.toLowerCase().equals("favicon.ico")) {
            return this.getFaviconResource(location);
        }

        return null;
    }

    /*
     * Serve HTML for our Single Page App, for a request such as 'spa/css/app.css'
     */
    protected Resource getSinglePageAppResource(final String resourcePath, final Resource location) {

        // The web configuration file is a special case
        if (resourcePath.toLowerCase().contains("spa.config.json")) {

            // When the spa.config.json file is requested, we serve the local API version
            var physicalPath = String.format("%s/spa.config.localapi.json", spaRootLocation);
            return this.getResourceFromPhysicalPath(physicalPath, location);
        }

        // Handle requests for general web resources next
        if (resourcePath.toLowerCase().startsWith("spa/")) {

            // Serve the resource from a path such as 'file:../authguidance.websample.final/css/app.css'
            final var prefixLength = 4;
            var physicalPath = String.format("%s/%s", spaRootLocation, resourcePath.substring(prefixLength));
            var resource = this.getResourceFromPhysicalPath(physicalPath, location);
            if (resource != null) {
                return resource;
            }
        }

        // Fall back to serving the index.html resource for any not found resources
        var indexPhysicalPath = String.format("%s/index.html", spaRootLocation);
        return this.getResourceFromPhysicalPath(indexPhysicalPath, location);
    }

    /*
     * Serve HTML for our initial Desktop Sample's post login page
     */
    protected Resource getLoopbackResource(final String resourcePath, final Resource location) {

        // Serve the post login page from the path 'file:../authguidance.desktopsample1/web/postlogin.html'
        if (resourcePath.toLowerCase().contains("loopback/postlogin.html")) {

            var loginPhysicalPath = String.format("%s/postlogin.html", this.loopbackRootLocation);
            return this.getResourceFromPhysicalPath(loginPhysicalPath, location);
        }

        return null;
    }

    /*
     * Serve HTML for our desktop sample's post login and post logout pages
     */
    protected Resource getDesktopResource(final String resourcePath, final Resource location) {

        // Serve the post login page from the path 'file:../authguidance.desktopsample.final/web/postlogin.html'
        if (resourcePath.toLowerCase().contains("desktop/postlogin.html")) {

            var loginPhysicalPath = String.format("%s/postlogin.html", this.desktopRootLocation);
            return this.getResourceFromPhysicalPath(loginPhysicalPath, location);
        }

        // Serve the post logout page from the path 'file:../authguidance.desktopsample.final/web/postlogout.html'
        if (resourcePath.toLowerCase().contains("desktop/postlogout.html")) {

            var logoutPhysicalPath = String.format("%s/postlogout.html", this.desktopRootLocation);
            return this.getResourceFromPhysicalPath(logoutPhysicalPath, location);
        }

        return null;
    }

    /*
     * Serve HTML for our Android sample's interstitial pages
     */
    protected Resource getAndroidResource(final String resourcePath, final Resource location) {

        // Serve the post login page from the path 'file:../authguidance.mobilesample.android/web/postlogin.html'
        if (resourcePath.toLowerCase().contains("android/postlogin.html")) {

            var loginPhysicalPath = String.format("%s/postlogin.html", this.androidRootLocation);
            return this.getResourceFromPhysicalPath(loginPhysicalPath, location);
        }

        // Serve the post logout page from the path 'file:../authguidance.mobilesample.android/web/postlogout.html'
        if (resourcePath.toLowerCase().contains("android/postlogout.html")) {

            // Serve it from the web folder of the mobile sample
            var logoutPhysicalPath = String.format("%s/postlogout.html", this.androidRootLocation);
            return this.getResourceFromPhysicalPath(logoutPhysicalPath, location);
        }

        return null;
    }

    /*
     * Serve HTML for our iOS sample's interstitial pages
     */
    protected Resource getIosResource(final String resourcePath, final Resource location) {

        // Serve the post login page from the path 'file:../authguidance.mobilesample.ios/web/postlogin.html'
        if (resourcePath.toLowerCase().contains("ios/postlogin.html")) {

            var loginPhysicalPath = String.format("%s/postlogin.html", this.iosRootLocation);
            return this.getResourceFromPhysicalPath(loginPhysicalPath, location);
        }

        // Serve the post logout page from the path 'file:../authguidance.mobilesample.ios/web/postlogout.html'
        if (resourcePath.toLowerCase().contains("ios/postlogout.html")) {

            // Serve it from the web folder of the mobile sample
            var logoutPhysicalPath = String.format("%s/postlogout.html", this.iosRootLocation);
            return this.getResourceFromPhysicalPath(logoutPhysicalPath, location);
        }

        return null;
    }

    /*
     * Serve the favicon.ico file
     */
    protected Resource getFaviconResource(final Resource location) {

        var faviconPhysicalPath = String.format("%s/favicon.ico", spaRootLocation);
        return this.getResourceFromPhysicalPath(faviconPhysicalPath, location);
    }

    /*
     * A utility to load a resource from its physical path
     */
    private Resource getResourceFromPhysicalPath(final String physicalPath, final Resource location) {

        try {

            var requestedResource = location.createRelative(physicalPath);
            if (requestedResource.exists() && requestedResource.isReadable()) {
                return requestedResource;
            }
        } catch (IOException ex) {

            var message = String.format("IOException serving Android web content for %s", physicalPath);
            throw new RuntimeException(message, ex);
        }

        return null;
    }
}
