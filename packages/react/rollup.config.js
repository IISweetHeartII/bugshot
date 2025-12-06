import typescript from '@rollup/plugin-typescript';
import resolve from '@rollup/plugin-node-resolve';
import commonjs from '@rollup/plugin-commonjs';

export default [
  {
    input: 'src/index.tsx',
    output: {
      file: 'dist/index.esm.js',
      format: 'es',
      sourcemap: true,
    },
    external: ['react', '@bugshot/browser-sdk'],
    plugins: [
      resolve(),
      commonjs(),
      typescript({
        tsconfig: './tsconfig.json',
        declaration: true,
        declarationDir: './dist',
      }),
    ],
  },
  {
    input: 'src/index.tsx',
    output: {
      file: 'dist/index.cjs.js',
      format: 'cjs',
      sourcemap: true,
    },
    external: ['react', '@bugshot/browser-sdk'],
    plugins: [
      resolve(),
      commonjs(),
      typescript({
        tsconfig: './tsconfig.json',
        declaration: false,
      }),
    ],
  },
];
