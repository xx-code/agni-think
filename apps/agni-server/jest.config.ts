// /** @type {import('ts-jest').JestConfigWithTsJest} **/
// module.exports = {
//   testEnvironment: "node",
//   transform: {
//     "^.+\.tsx?$": ["ts-jest",{}],
//   },
// };

import type { Config } from "@jest/types"


const config: Config.InitialOptions = {
  preset: "ts-jest",
  testEnvironment: "node",
  moduleNameMapper: {
    '^@core/(.*)$': '<rootDir>/src/core/$1',
  },
  transform: {
    '^.+\\.ts?$': 'ts-jest',
    '^.+\\.tsx?$': 'ts-jest',
    '^.+\\.js?$': 'babel-jest',
    '^.+\\.jsx?$': 'babel-jest',
  }
}
export default config