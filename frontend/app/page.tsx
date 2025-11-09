export default function Home() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="text-center">
        <h1 className="text-6xl font-bold text-gray-900 mb-4">
          Error Monitor
        </h1>
        <p className="text-xl text-gray-600 mb-8">
          다국어 지원 에러 모니터링 서비스
        </p>
        <div className="space-x-4">
          <button className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
            시작하기
          </button>
          <button className="px-6 py-3 bg-white text-gray-700 rounded-lg border border-gray-300 hover:bg-gray-50 transition">
            문서 보기
          </button>
        </div>
      </div>
    </div>
  );
}
