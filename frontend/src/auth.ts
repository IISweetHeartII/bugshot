import NextAuth from "next-auth";
import GitHub from "next-auth/providers/github";
import Google from "next-auth/providers/google";

export const { handlers, signIn, signOut, auth } = NextAuth({
  providers: [
    GitHub({
      clientId: process.env.GITHUB_CLIENT_ID || "",
      clientSecret: process.env.GITHUB_CLIENT_SECRET || "",
    }),
    Google({
      clientId: process.env.GOOGLE_CLIENT_ID || "",
      clientSecret: process.env.GOOGLE_CLIENT_SECRET || "",
    }),
  ],
  pages: {
    signIn: "/login",
    error: "/login",
  },
  callbacks: {
    async signIn({ user, account }) {
      // Send user info to backend for registration/login
      // Note: This runs server-side, so we use BACKEND_URL (not NEXT_PUBLIC_*)
      try {
        const response = await fetch(`${process.env.BACKEND_URL}/api/auth/oauth`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            provider: account?.provider,
            providerId: account?.providerAccountId,
            email: user.email,
            name: user.name,
            image: user.image,
          }),
        });

        if (!response.ok) {
          console.error("Failed to register user with backend");
          return false;
        }

        // Get backend user ID from response and store it
        const data = await response.json();
        if (data.success && data.data?.user?.id) {
          // Store backend user ID for jwt callback
          (user as any).backendId = data.data.user.id;
        }

        return true;
      } catch (error) {
        console.error("Error during sign in:", error);
        return false;
      }
    },
    async jwt({ token, user, account }) {
      // Persist the backend user ID and provider to the token
      if (account && user) {
        (token as any).provider = account.provider;
        (token as any).backendId = (user as any).backendId || user.id;
      }
      return token;
    },
    async session({ session, token }) {
      // Send properties to the client
      return {
        ...session,
        user: {
          ...session.user,
          id: (token as any).backendId || "",
          provider: (token as any).provider || "",
        },
      };
    },
  },
  session: {
    strategy: "jwt",
  },
  secret: process.env.NEXTAUTH_SECRET,
});
