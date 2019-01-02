const path = require('path');
const webpack = require('webpack');

module.exports = {
  
  // Working folder
  context: path.resolve(__dirname, 'src'),

  // Pull in all dependencies starting from the root file
  entry: ["./plumbing/polyfill.ts", "./logic/app.ts"],
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
    new webpack.optimize.CommonsChunkPlugin({
      
      // Build 3rd party code into a Vendor bundle file
      name: 'vendor',
      filename: '../dist/vendor.bundle.min.js',
      minChunks (module) {
          return module.context && module.context.indexOf('node_modules') !== -1;
      }
    })
  ]
}