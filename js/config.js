/**
 * EduConnect - Application Configuration & Master Constants
 * Author: Senior Frontend Developer (CSP Team)
 * Integration target: Spring Boot REST API + MySQL
 */

const CONFIG = {
  // Spring Boot API Base URL (Configurable for local dev / production)
  API_BASE_URL: 'http://localhost:8080/api',

  // Request timeout in milliseconds
  REQUEST_TIMEOUT_MS: 12000,

  // Local storage keys
  STORAGE_KEYS: {
    AUTH_TOKEN: 'educonnect_auth_token',
    USER_DATA: 'educonnect_user_data',
    LOCAL_BOOKMARKS: 'educonnect_local_bookmarks',
    DEMO_MODE: 'educonnect_demo_mode'
  },

  // Academic Categories
  CATEGORIES: [
    { id: 'notes', label: 'Subject Notes', badgeClass: 'badge-notes', icon: 'notes' },
    { id: 'qp', label: 'Previous Question Papers', badgeClass: 'badge-qp', icon: 'qp' },
    { id: 'lab', label: 'Lab Manuals', badgeClass: 'badge-lab', icon: 'lab' },
    { id: 'prog', label: 'Programming Resources', badgeClass: 'badge-prog', icon: 'prog' },
    { id: 'tutorials', label: 'Tutorials & Reference Materials', badgeClass: 'badge-tutorial', icon: 'tutorials' },
    { id: 'other', label: 'Other Study Materials', badgeClass: 'badge-other', icon: 'other' }
  ],

  // Academic Departments
  DEPARTMENTS: [
    'Computer Science & Engineering',
    'Information Technology',
    'Electronics & Communication Engineering',
    'Electrical & Electronics Engineering',
    'Mechanical Engineering',
    'Civil Engineering',
    'Data Science & AI',
    'Basic Sciences & Humanities'
  ],

  // Academic Semesters
  SEMESTERS: [
    'Semester 1',
    'Semester 2',
    'Semester 3',
    'Semester 4',
    'Semester 5',
    'Semester 6',
    'Semester 7',
    'Semester 8'
  ],

  // Accepted File Formats & Max Size (Agreed with backend team)
  UPLOAD_CONSTRAINTS: {
    MAX_FILE_SIZE_BYTES: 25 * 1024 * 1024, // 25 MB
    MAX_FILE_SIZE_LABEL: '25 MB',
    ACCEPTED_EXTENSIONS: ['.pdf', '.docx', '.pptx', '.zip', '.txt'],
    ACCEPTED_MIME_TYPES: [
      'application/pdf',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.presentationml.presentation',
      'application/zip',
      'text/plain'
    ]
  },

  // Demo Accounts for CSP Evaluation / Presentation
  DEMO_ACCOUNTS: {
    STUDENT: {
      email: 'student@college.edu',
      password: 'Password123!',
      fullName: 'Aarav Sharma',
      department: 'Computer Science & Engineering',
      semester: 'Semester 6',
      role: 'ROLE_STUDENT'
    },
    ADMIN: {
      email: 'admin@college.edu',
      password: 'AdminPassword123!',
      fullName: 'Prof. Lakshmi Rao (CSP Coordinator)',
      department: 'Computer Science & Engineering',
      semester: 'Faculty / Admin',
      role: 'ROLE_ADMIN'
    }
  },

  // Clearly separated sample dataset for initial frontend preview/demo
  SAMPLE_RESOURCES: [
    {
      id: 'res-101',
      title: 'Database Management Systems - Comprehensive Unit 1-5 Lecture Notes',
      description: 'Complete syllabus coverage of Relational Data Models, SQL Queries, Normalization (1NF through BCNF), and Transaction Processing with solved university exam problems.',
      category: 'notes',
      categoryLabel: 'Subject Notes',
      department: 'Computer Science & Engineering',
      semester: 'Semester 4',
      subject: 'Database Management Systems',
      fileType: 'PDF',
      fileSize: '4.2 MB',
      uploadDate: '2026-03-15',
      contributorName: 'Priya Patel',
      isApproved: true,
      downloadCount: 342,
      isSample: true
    },
    {
      id: 'res-102',
      title: 'Operating Systems - End Semester Solved Question Papers (2021-2025)',
      description: 'Compiled university question papers with model step-by-step solutions covering CPU scheduling algorithms, Deadlock detection, Virtual Memory paging, and POSIX multithreading.',
      category: 'qp',
      categoryLabel: 'Previous Question Papers',
      department: 'Computer Science & Engineering',
      semester: 'Semester 4',
      subject: 'Operating Systems',
      fileType: 'PDF',
      fileSize: '6.8 MB',
      uploadDate: '2026-03-10',
      contributorName: 'Rahul Verma',
      isApproved: true,
      downloadCount: 512,
      isSample: true
    },
    {
      id: 'res-103',
      title: 'Data Structures & Algorithms Laboratory Manual with C++ & Java Code',
      description: 'Complete laboratory manual featuring balanced BSTs (AVL, Red-Black), Dijkstra algorithm, Floyd-Warshall, Dynamic Programming, and complexity analysis checklists.',
      category: 'lab',
      categoryLabel: 'Lab Manuals',
      department: 'Information Technology',
      semester: 'Semester 3',
      subject: 'Data Structures Lab',
      fileType: 'PDF',
      fileSize: '3.1 MB',
      uploadDate: '2026-03-02',
      contributorName: 'Aarav Sharma',
      isApproved: true,
      downloadCount: 289,
      isSample: true
    },
    {
      id: 'res-104',
      title: 'Spring Boot 3.x & RESTful API Architecture Quick Reference Sheet',
      description: 'Hands-on guide to Spring MVC, Dependency Injection, JPA repository queries, DTO patterns, Exception Handling with ControllerAdvice, and JWT authentication flows.',
      category: 'prog',
      categoryLabel: 'Programming Resources',
      department: 'Computer Science & Engineering',
      semester: 'Semester 6',
      subject: 'Web Technologies & Frameworks',
      fileType: 'PDF',
      fileSize: '2.4 MB',
      uploadDate: '2026-02-28',
      contributorName: 'Sneha Reddy',
      isApproved: true,
      downloadCount: 420,
      isSample: true
    },
    {
      id: 'res-105',
      title: 'Digital Signal Processing - Fast Fourier Transform & Filter Design Notes',
      description: 'Detailed derivations and numerical exercises for DFT, Radix-2 FFT algorithms, FIR filter design using window techniques, and IIR Chebyshev / Butterworth filters.',
      category: 'notes',
      categoryLabel: 'Subject Notes',
      department: 'Electronics & Communication Engineering',
      semester: 'Semester 5',
      subject: 'Digital Signal Processing',
      fileType: 'PDF',
      fileSize: '5.5 MB',
      uploadDate: '2026-02-20',
      contributorName: 'Karthik Nair',
      isApproved: true,
      downloadCount: 195,
      isSample: true
    },
    {
      id: 'res-106',
      title: 'Computer Networks - Wireshark Packet Sniffing & Socket Programming Tutorial',
      description: 'Practical tutorial covering TCP 3-way handshake analysis, DNS queries, HTTP/2 inspection using Wireshark, plus complete client-server socket code.',
      category: 'tutorials',
      categoryLabel: 'Tutorials & Reference Materials',
      department: 'Information Technology',
      semester: 'Semester 5',
      subject: 'Computer Networks',
      fileType: 'PDF',
      fileSize: '4.8 MB',
      uploadDate: '2026-02-14',
      contributorName: 'Ananya Deshmukh',
      isApproved: true,
      downloadCount: 310,
      isSample: true
    },
    {
      id: 'res-107',
      title: 'Artificial Intelligence - Search Algorithms & Game Playing (A*, Minimax with Alpha-Beta)',
      description: 'Comprehensive study material on state space search, heuristic functions, admissible heuristics, and adversarial search games with worked trace examples.',
      category: 'notes',
      categoryLabel: 'Subject Notes',
      department: 'Data Science & AI',
      semester: 'Semester 5',
      subject: 'Artificial Intelligence',
      fileType: 'PDF',
      fileSize: '3.7 MB',
      uploadDate: '2026-01-28',
      contributorName: 'Rohan Mehra',
      isApproved: false, // Pending approval for admin demo!
      downloadCount: 0,
      isSample: true
    }
  ]
};

// Freeze configuration to prevent accidental mutations
Object.freeze(CONFIG);
