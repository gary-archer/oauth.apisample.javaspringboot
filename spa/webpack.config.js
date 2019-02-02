const path = require('path');
const webpack = require('webpack');

module.exports = {
  
  // Working folder
  context: path.resolve(__dirname, 'src'),

  // Pull in all dependencies starting from the root file
  entry: ["./plumbing/polyfill.ts", "./app.ts"],
  output: {
    
    // Build our code into an SPA bundle file
    path: path.resolve(__dirname, 'dist'),
    filename: 'spa.bundle.min.js'
  },
  resolve: {
    
    // Set extensions for import statements
    extensions: ['.ts']
  },
  module: {
    rules: [
      {
        // Files with a .ts extension are loaded by the Typescript loader
        test: /\.ts$/, 
        loader: 'ts-loader', 
      }
    ]
  },
  plugins: [
    
    // Build 3rd party code into a Vendor bundle file
    new webpack.optimize.CommonsChunkPlugin({
      name: 'vendor',
      filename: '../dist/vendor.bundle.min.js',
      minChunks (module) {
          return module.context && module.context.indexOf('node_modules') !== -1;
      }
    }),

    // Disable moment time formatting locales, which causes problems and my sample does not need
    // https://github.com/moment/moment/issues/4945
    // https://github.com/jmblog/how-to-optimize-momentjs-with-webpack
    new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/)
  ]
}