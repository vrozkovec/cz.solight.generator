<?php
/**
 * Case permutation file finder
 * Tries all case combinations until a match is found
 * PHP 5.6 compatible
 */

// Configuration
$baseDir = __DIR__ . '/obrazky';
$baseUrl = 'https://obrazky.generator.solight.cz/obrazky';
$allowedExtensions = array('jpg', 'jpeg', 'png', 'gif', 'webp', 'svg');

// Get requested filename
$requested = isset($_GET['image']) ? $_GET['image'] : null;

if (!$requested) {
    http_response_code(400);
    exit('Missing image parameter');
}

// Security: prevent directory traversal
$requested = basename($requested);

// Validate extension
$ext = strtolower(pathinfo($requested, PATHINFO_EXTENSION));
if (!in_array($ext, $allowedExtensions)) {
    http_response_code(403);
    exit('File type not allowed');
}

/**
 * Generate all case permutations of a string
 * 
 * @param string $str
 * @return Generator
 */
function casePermutations($str)
{
    $length = strlen($str);
    
    // Find positions of letters (not digits, dots, etc.)
    $letterPositions = array();
    for ($i = 0; $i < $length; $i++) {
        if (ctype_alpha($str[$i])) {
            $letterPositions[] = $i;
        }
    }
    
    $letterCount = count($letterPositions);
    $totalCombinations = 1 << $letterCount; // 2^letterCount
    
    for ($mask = 0; $mask < $totalCombinations; $mask++) {
        $variant = $str;
        for ($i = 0; $i < $letterCount; $i++) {
            $pos = $letterPositions[$i];
            if ($mask & (1 << $i)) {
                $variant[$pos] = strtoupper($variant[$pos]);
            } else {
                $variant[$pos] = strtolower($variant[$pos]);
            }
        }
        yield $variant;
    }
}

// Limit for safety
$letterCount = 0;
for ($i = 0; $i < strlen($requested); $i++) {
    if (ctype_alpha($requested[$i])) {
        $letterCount++;
    }
}
if ($letterCount > 50) {
    http_response_code(400);
    exit('Filename too long for case search');
}

// Try all case permutations
$found = null;
foreach (casePermutations($requested) as $variant) {
    if (file_exists($baseDir . '/' . $variant)) {
        $found = $variant;
        break;
    }
}

if (!$found) {
    http_response_code(404);
    exit('File not found');
}

http_response_code(200);
exit($baseUrl . '/' . rawurlencode($found));
