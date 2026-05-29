package com.example.data

object DefaultKnowledgeBase {
    fun getDefaultDocuments(): List<Document> {
        return listOf(
            Document(
                title = "Standard Platform Refund Policy",
                category = "Payment Gateway",
                tags = "refunds, refund policy, billing, cancellations, subscription, transaction",
                content = "What is the refund policy? Our standard platform allows users to request a full customer reimbursement refund within 14 calendar days of physical checkout or subscription signup. Subscriptions can be canceled instantly from client settings, after which automatic payment processing terminates. Refunds are processed back to the original funding card within 5 to 10 banking days."
            ),
            Document(
                title = "Self-Service Password Reset Instructions",
                category = "Account Settings",
                tags = "login, password reset, credential recovery, forgot password, account security",
                content = "How do I reset my password? To recover your portal profile and reset your login password, go to the main login form and click 'Forgot Password?'. Fill in your verified email account text field, and check your inbox for an automated validation link containing secure account restoration credentials."
            ),
            Document(
                title = "Payment Processing Failed Troubleshooting",
                category = "Payment Gateway",
                tags = "payment failures, checkout errors, gateway timeout, transaction declined",
                content = "What should I do if payment fails? If checking out triggers transaction decline alerts or billing errors, follow these steps: 1) Double-check your credit card expiration, CSV security number, and standard zip code billing fields; 2) Try checking out using a different card or web browser; 3) Contact your credit union to verify daily payment bounds."
            ),
            Document(
                title = "Connecting with Customer Support Teams",
                category = "Customer Support",
                tags = "customer support, support contact, helpdesk, live chat, ticketing, help",
                content = "How can I contact customer support? Users can connect with our 24/7 technical client assistance desk by emailing support@company.com or initiating the live support dialog chat. Enterprise tier users can open priority tickets and initiate voice channels directly from their partner dashboards."
            ),
            Document(
                title = "Onboarding Timeline and Requirements",
                category = "Platform Onboarding",
                tags = "onboarding, signup progress, setup duration, identification, verification",
                content = "How long does onboarding take? The standardized client onboarding procedure takes between 2 to 4 hours, which covers automated software settings, database parameters mapping, and account registration. However, corporate manual identity checking can take up to 3 business days if verification triggers compliance reviews."
            ),
            Document(
                title = "Solving Account Login Obstacles",
                category = "Authentication Issue",
                tags = "login issues, cannot sign in, login failures, credential block, locked account, dashboard problems",
                content = "I cannot log into my account. If you encounter sign-in failures, browser session cookie hangs, or credential denials, verify your Caps Lock key is status off and search your mail archives for password reset warnings. Try clearing system cookies or using an incognito private window to flush stale authentication headers."
            ),
            Document(
                title = "Resolving Authentication Loop Errors",
                category = "Authentication Issue",
                tags = "authentication, auth failures, credentials, expired session, login issues",
                content = "My authentication keeps failing. When requests throw continuous authorization blocks, it means your local auth key has expired or your browser has blocked third-party tracking. Reset your integration keys inside the Account section or sign out and sign back in to renew cryptographic JWT session credentials."
            ),
            Document(
                title = "Preventing Rest API Connection Timeouts",
                category = "API Integration",
                tags = "API errors, latency, network timeouts, endpoint slow, response delay",
                content = "Why is the API timing out? API request latency and REST endpoint timeouts typically happen under high payload concurrency or unoptimized indexing queries. Adjust client-side HTTP timeouts to 60 seconds, configure pagination parameters on large list responses, and verify server load metrics."
            ),
            Document(
                title = "Payment Gateway Unresponsive Diagnostics",
                category = "Payment Gateway",
                tags = "payment failures, gateway unresponding, checkout failure, checkout stuck, API errors",
                content = "Payment gateway is not responding. When checking out stalls on checkout payment screens, check whether your outbound network blocks external payment processors or if the integration is suffering a billing partner crash. Try disabling adblock extensions before processing payment again."
            ),
            Document(
                title = "Recovering Forgotten Username Credentials",
                category = "Account Settings",
                tags = "login, credentials, forgot password, credentials recovery, security lookup",
                content = "I forgot my login credentials. If you are locked out because of missing login handles, navigate to credentials search page. Confirm your secondary security address to instantly obtain an SMS or email listing your registered username details securely."
            ),
            Document(
                title = "Corporate and Agency Partnerships Ecosystem",
                category = "Business Partnership",
                tags = "partnerships, business association, reseller contract, lead referrals, integration",
                content = "Show information related to partnerships. Our corporate partnership tier unlocks agency cooperation levels, referral payout logs, and access to customized RAG integrations. Partners receive structured technical setups and custom APIs to register corporate leads seamlessly."
            ),
            Document(
                title = "Recruiting External Contractors and Engineers",
                category = "Business Partnership",
                tags = "contractors, external team, timesheet billing, project outsourcing",
                content = "Any feedback regarding contractors? Reviews of contracted remote developers indicate high delivery performance and precise technical troubleshooting metrics. Contractors are bound by strict NDA scopes, logging all operational codes inside sandbox environments."
            ),
            Document(
                title = "Onboarding Setup Integration Problems",
                category = "Platform Onboarding",
                tags = "onboarding, friction, identity verification, signup, onboarding issues, setup delay",
                content = "What issues are related to onboarding? Onboarding issues regularly reported include cellular token delivery delays, fuzzy identity document scan rejections, and unclear database setup documentation. Users recommend self-service sandbox guides to smooth the setup friction."
            ),
            Document(
                title = "Resolving API Key Authorization Failures",
                category = "API Integration",
                tags = "API errors, debugging, bearer token, authorization, authentication, API failures",
                content = "Search for API-related problems. Standard API failures stem from incorrectly formatted Authorization Bearer headers or expired client keys. Developers can troubleshoot integration faults by inspecting the real-time API logs inside their developer console."
            ),
            Document(
                title = "Managing Referrals and Affiliate Commission",
                category = "Business Partnership",
                tags = "lead referrals, payouts, partnerships, affiliate tracking, lead payouts",
                content = "Find entries about lead referrals. Our partner referral strategy commissions registered affiliates 20% on closed leads. Referral leads must sign up using unique tracking cookies, and balances are paid out with 30-day clearing windows once surpassing $100 limits."
            ),
            Document(
                title = "Analysing Common Card Processing Failures",
                category = "Payment Gateway",
                tags = "payment failures, card decline, checkout billing, zip mismatch, common payment issues",
                content = "What are the common payment issues users face? Active transactions suffer failure due to: 1) False-positive fraud flags on foreign currency checkouts; 2) Insufficient credit limits on corporate credit profiles; 3) Mismatched zip billing addresses."
            ),
            Document(
                title = "Evaluating Captcha Loop Friction",
                category = "Authentication Issue",
                tags = "login issues, captcha loops, MFA delays, login systems, complaints, user complaints",
                content = "Summarize customer complaints about login systems. A compilation of customer grievances about authentication reveals that captcha validation loops and delayed SMS MFA confirmation numbers are the greatest sources of user logging dissatisfaction and complaints."
            ),
            Document(
                title = "Mapping Onboarding Setup Pain Points",
                category = "Platform Onboarding",
                tags = "onboarding, signup progress, registration delay, onboarding feedback, onboarding issues",
                content = "What onboarding problems are most frequently reported? The most recurrent setup issues include: 1) Manual administrative approval lags; 2) Phone verification SMS failing on non-domestic carriers; 3) Difficulty binding legacy billing systems to client panels."
            ),
            Document(
                title = "Assessing OAuth Integration Failures",
                category = "API Integration",
                tags = "integrations, oauth connectivity, webhook failure, stale tokens, user complaints",
                content = "What feedback exists about integrations? Development teams highlight integration issues with third-party webhooks, such as silent package losses, stale OAuth synchronization keys, and lack of visual debugger testing tools inside client dashboards."
            ),
            Document(
                title = "Security Credentials & Authentication Threats",
                category = "Authentication Issue",
                tags = "authentication, oauth2, secure login, auth concerns, compromised keys, login issues",
                content = "Explain the major authentication-related concerns. Dominant security issues focus on credentials safety and token leaks. Enabling strict MFA, rotating API integration keys regularly, and avoiding open text storage of login credits are recommendations to secure accounts."
            ),
            Document(
                title = "Disputed Charges & Refund Timeline",
                category = "Payment Gateway",
                tags = "refunds, cancellations, customer reimbursement, dispute, billing",
                content = "Search for refund-related entries. Our database has refund logs specifying that 94% of reimbursement cancellations are approved within 2 business days. Disputed checkouts require billing manual inspection, extending payment refund wait times to 12 days."
            ),
            Document(
                title = "Contractor Workspace Permissions",
                category = "Business Partnership",
                tags = "contractors, developers, workspace access, secure endpoints, permissions",
                content = "Find information about external contractors. External contractors must be assigned custom roles with restricted database scopes. Contractor credentials automatically expire after 180 days unless renewed by administrative integration panels."
            ),
            Document(
                title = "Partner Agency Tier SLA",
                category = "Business Partnership",
                tags = "partnerships, agency agreement, corporate SLA, corporate tiers",
                content = "What data is available regarding agency partnerships? Agency partnership documents define service level agreement (SLA) metrics, with Gold tiers guaranteed 99.99% system availability while receiving specialized custom API endpoints and visual analytics panels."
            ),
            Document(
                title = "Authentication Vector Token Renewal",
                category = "Authentication Issue",
                tags = "authentication, tokens, login credentials, credential refresh, login issues",
                content = "Show entries tagged with authentication. Authenticated users are assigned standard JSON Web Tokens (JWT) that expire hourly. To secure sessions against hijacking, automatic client refreshes retrieve active auth headers in the background without user interruption."
            ),
            Document(
                title = "Testing Vector Search Embedding Sizes",
                category = "Vector Search RAG",
                tags = "vector embeddings, semantic search, cosine similarity, dimensional check",
                content = "Retrieve answers about vector embeddings. Vector embeddings convert text data into high-dimensional float vectors (typically 768 dimensions). Semantic search processes queries by calculating cosine similarity distances between inputs and stored documents in memory."
            ),
            Document(
                title = "Overcoming Inbound Payment Transaction Failures",
                category = "Payment Gateway",
                tags = "payment failures, payment declined, transaction, gateway timeout, billing issues",
                content = "Can you help me with payment issues? Credit card payment transaction declined blocks are easily resolved by toggling international payments on with your card issuer, checking account balance levels, or canceling active ad-blocking tools on checkout views."
            ),
            Document(
                title = "Resolving Setup Document Upload Halts",
                category = "Platform Onboarding",
                tags = "onboarding, setup, document upload, verification, onboarding issues",
                content = "I am facing problems during onboarding. If onboarding document uploads freeze or trigger verification rejects, verify file inputs occupy less than 5MB sizing caps, are saved in PDF/PNG formats, and display high-contrast uncropped scans of identity papers."
            ),
            Document(
                title = "Explaining Portal Authentication Obstacles",
                category = "Authentication Issue",
                tags = "login issues, signup, system down, CAPTCHA error, login system, user complaints",
                content = "Users are complaining about login failures — what are the reasons? Detailed technical reviews of user logging grievances point to browser caching glitches, Safari local storage locks, and temporary server maintenance slowdowns during peak access."
            ),
            Document(
                title = "Handling API Response Failures and Rate-Limits",
                category = "API Integration",
                tags = "API errors, API failure, rate limit, http 429, API solutions",
                content = "What solutions exist for API failures? API rate blocks (Http 429) require implementing automatic exponential backoff retry algorithms, compressing massive transfer payloads below 2MB limits, and pooling duplicate REST connection queries."
            ),
            Document(
                title = "Optimizing Support Ticket Response Sprints",
                category = "Customer Support",
                tags = "customer support, support team, ticketing speed, support solutions",
                content = "Are there any recommendations regarding customer support? We advise introducing automated diagnostic bots to evaluate simple checkout and login queries instantly, routing complex troubleshooting tickets directly to engineers via priority channels."
            ),
            Document(
                title = "Frequent Platform Issues Metrics",
                category = "Dashboard Analytics",
                tags = "customer complaints, most frequent issues, analytics, metrics, database trends, dashboard problems",
                content = "Which issue appears most frequently in the database? Database ticket analysis reveals that login failures/Authentication issues are the most frequent platform complaint (representing 38% of records), followed by transaction failures (24%)."
            ),
            Document(
                title = "Top Customer Pain Points Overview",
                category = "Customer Support",
                tags = "customer complaints, customer pain points, top customer pain points, analytics",
                content = "What are the top customer pain points in our platform? 1) Login failure because of SMS multi-factor token latency; 2) Long onboarding duration due to manual identity review; 3) Checkout systems hanging due to payment gateway unresponsive timeouts."
            ),
            Document(
                title = "Webhook Connectivity Feedback Summary",
                category = "API Integration",
                tags = "integrations, database, webhook performance, integrations feedback",
                content = "Summarize all feedback related to integrations. User integration complaints cite webhooks packet loss under spikes and complex JWT registration rules. Resolving webhooks delays requires deploying highly scalable serverless queues."
            ),
            Document(
                title = "Bi-Weekly Customer Satisfaction Analytics Reporting",
                category = "Dashboard Analytics",
                tags = "customer complaints, feedback trends, user dissatisfaction, analytics, dashboard problems",
                content = "What trends are visible in user complaints? Quarterly customer grievance logs report a 15% increase in mobile checkout declines and growing complaints regarding slower dashboard analytics widgets on legacy mobile browsers."
            ),
            Document(
                title = "Database Tag Distribution Frequency",
                category = "Dashboard Analytics",
                tags = "database, analytics, tag frequency, metadata performance, metrics",
                content = "Which tags occur most often in the document database? Metadata metrics show 'authentication' is the absolute most frequent tag (utilized 15 times), followed closely by 'payment failures' (12 entries), and 'onboarding' (9 entries)."
            ),
            Document(
                title = "Comprehensive Onboarding Diagnostic Compilation",
                category = "Platform Onboarding",
                tags = "onboarding, user progress, signup friction, onboarding feedback",
                content = "Give a complete summary of all onboarding-related feedback. Onboarding feedback compiles user praise for clear setup layout wizards but registers friction points on document scanning verification steps and delayed confirmation emails."
            ),
            Document(
                title = "Self-Service Manual for Authentication Interruptions",
                category = "Authentication Issue",
                tags = "authentication, password recovery, lockout, authenticator, credential fixes, login issues",
                content = "List all authentication issues with possible solutions: 1) Lockouts: Wait for 30 minutes for security clearance; 2) Expired Session: Log out and re-login; 3) Missing Reset Links: Whitelist system-generated email handles; 4) MFA delays: Migrate to app authenticators."
            ),
            Document(
                title = "Explaining Credit Card Transaction Failure Triggers",
                category = "Payment Gateway",
                tags = "payment failures, credit card exceptions, billing errors, checkout solutions",
                content = "Explain every payment-related problem found in the database. Documented payment issues are: 1) Card Declines due to AVS billing zip issues; 2) Payment Gateway Unresponsive conditions; 3) Double charge anomalies when clicking submit twice."
            ),
            Document(
                title = "Consolidated Engineering Troubleshooting Reports",
                category = "Dashboard Analytics",
                tags = "technical troubleshoot, system logs, API errors, dashboard lag, technical troubleshooting",
                content = "Summarize all technical issues users reported. Consolidated incident reports detail the slow dashboard widgets during vector updates, minor API authorization lags on expired JWT bearer tokens, and temporary webhook communication drops."
            ),
            Document(
                title = "Solving Slate Dashboard Loading Slowdowns",
                category = "Dashboard Analytics",
                tags = "dashboard problems, latency, performance, slow rendering, analytics charts",
                content = "Dashboard problems and slow rendering: If analytics charts load slowly, verify your web browser context supports active hardware acceleration. Rendering speeds can also improve by configuring custom date query range filters."
            ),
            Document(
                title = "Enterprise Application Deployment Pipelines",
                category = "Platform Deployment",
                tags = "deployment, pipelines, deployment fails, hosting, cloud sync",
                content = "When deployment pipelines throw integration failures, it usually signals environment key mismatches between local docker settings and remote container hosting pools. Ensure deployment target clusters are configured to auto-retry on server resets."
            ),
            Document(
                title = "Managing Push Notification Latency",
                category = "Dashboard Analytics",
                tags = "notifications, push alerts, sync delays, mobile panels",
                content = "If notification delays crop up, check your cloud messaging certificates and register your application bundle with Google Play Services. Network drops are bypassed by configuring reliable backend retry intervals for mobile systems."
            ),
            Document(
                title = "Billing Invoice Generation Lag",
                category = "Account Settings",
                tags = "billing, billing issues, invoice lag, payment details",
                content = "Common billing issues concern delay of automated invoice delivery via email. Users can instantly fetch billing PDF files on accounts settings pages, select consolidated billing profiles, and update target tax registration ID fields directly."
            ),
            Document(
                title = "Semantic Search Relevance Tuning",
                category = "Vector Search RAG",
                tags = "semantic search, vector retrieval, similarity, confidence, RAG tuning",
                content = "To improve semantic search accuracy, developers should sanitize query tokens, strip common nouns, and adjust top-K filters. Combining local TF-IDF text models with dense API embeddings ensures accurate relevance scoring under diverse vocabularies."
            ),
            Document(
                title = "User Complaints Dashboard Classification",
                category = "Dashboard Analytics",
                tags = "user complaints, grievance logs, user satisfaction, support metrics",
                content = "We process user complaints using automatic semantic tagging, grouping customer grievances by tags like 'login failures', 'stripe checkout issues', and 'onboarding registration delay'. Clean tags allow support departments to index problems."
            ),
            Document(
                title = "Strategic Business Integrations Rules",
                category = "Business Partnership",
                tags = "partnerships, integrations, business contracts, third party API",
                content = "Enterprise agreements mandate that strategic business integrations maintain robust security standards. System connections require OAuth handshake protocols, separate contractor portals, and continuous monitoring of billing gateway logs."
            ),
            Document(
                title = "External Contractors Onboarding Guidelines",
                category = "Platform Onboarding",
                tags = "onboarding, contractors, developer signup, workspace setup",
                content = "Onboarding external software contractors matches corporate staff procedures but enforces tighter security parameters. Contractors must finish standard authentication and log visual timesheets on the client's internal ticket management system."
            ),
            Document(
                title = "High Dimensional Vector Indexing Performance",
                category = "Vector Search RAG",
                tags = "vector embeddings, indexing, high dimensional, database performance",
                content = "Running semantic search with massive dataset volumes degrades memory when computing cosine similarity metrics. Group records inside flat indexes, precompute embeddings arrays off-peak, and check if your API keys are registered to high tier networks."
            ),
            Document(
                title = "Troubleshooting Payment Gateway Slowdowns",
                category = "Payment Gateway",
                tags = "payment failures, checkout lag, stripe delay, billing gateway",
                content = "We resolve common payment issues and slow Stripe checkouts by routing transactions across multiple regional server hubs. When checkout lag spikes, buyers should avoid tapping checkout buttons twice to bypass false duplicate purchases."
            ),
            Document(
                title = "Master Customer Support FAQ Directory",
                category = "Customer Support",
                tags = "customer support, standard FAQ, support helpdesk, support solutions",
                content = "Our standard helpdesk directory is designed to resolve frequent technical support tickets. We provide step-by-step guidance covering account logins, password credential recovery, refund rules, and API connection latency fixes."
            ),
            Document(
                title = "Technical Troubleshooting Reference Indexes",
                category = "Customer Support",
                tags = "technical troubleshooting, debugging logs, development help, platform failures",
                content = "For in-depth technical troubleshooting on core REST endpoints, engineers check server log files at monitoring.domain.com. Common anomalies include expired authorization tokens, database connection blocks, and unresolvable web routing targets."
            ),
            Document(
                title = "Active Account Management Procedures",
                category = "Account Settings",
                tags = "account management, profile update, data privacy, delete profile",
                content = "Platform account management guidelines let users modify their email details, connect multi-factor apps, and download profile backups. Profile termination requests take up to 3 business days to fully purge active databases."
            )
        )
    }
}
