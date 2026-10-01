// built from two TypeScript files, with the bundle's source map inline
//#region src/geom.ts
var Point = class Point {
	x;
	y;
	constructor(x, y) {
		this.x = x;
		this.y = y;
	}
	scale(k) {
		if (k === 0) throw new Error("boom from geom.ts");
		return new Point(this.x * k, this.y * k);
	}
};
//#endregion
//#region src/shape.ts
function grow(p, k) {
	return p.scale(k);
}
//#endregion
export { Point, grow };

//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiZml4dHVyZS5qcyIsIm5hbWVzIjpbXSwic291cmNlcyI6WyJzcmMvZ2VvbS50cyIsInNyYy9zaGFwZS50cyJdLCJzb3VyY2VzQ29udGVudCI6WyJleHBvcnQgY2xhc3MgUG9pbnQge1xuICBjb25zdHJ1Y3RvcihwdWJsaWMgeDogbnVtYmVyLCBwdWJsaWMgeTogbnVtYmVyKSB7fVxuICBzY2FsZShrOiBudW1iZXIpOiBQb2ludCB7XG4gICAgaWYgKGsgPT09IDApIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcihcImJvb20gZnJvbSBnZW9tLnRzXCIpO1xuICAgIH1cbiAgICByZXR1cm4gbmV3IFBvaW50KHRoaXMueCAqIGssIHRoaXMueSAqIGspO1xuICB9XG59XG4iLCJpbXBvcnQgeyBQb2ludCB9IGZyb20gXCIuL2dlb21cIjtcblxuZXhwb3J0IGludGVyZmFjZSBTaXplZCB7IHNpemU6IG51bWJlciB9XG5cbmV4cG9ydCBmdW5jdGlvbiBncm93KHA6IFBvaW50LCBrOiBudW1iZXIpOiBQb2ludCB7XG4gIGNvbnN0IHNjYWxlZCA9IHAuc2NhbGUoayk7XG4gIHJldHVybiBzY2FsZWQ7XG59XG4iXSwibWFwcGluZ3MiOiI7O0FBQUEsSUFBYSxRQUFiLE1BQWEsTUFBTTtDQUNFO0NBQWtCO0NBQXJDLFlBQVksR0FBa0IsR0FBa0I7RUFBN0IsS0FBQSxJQUFBO0VBQWtCLEtBQUEsSUFBQTtDQUFZO0NBQ2pELE1BQU0sR0FBa0I7RUFDdEIsSUFBSSxNQUFNLEdBQ1IsTUFBTSxJQUFJLE1BQU0sbUJBQW1CO0VBRXJDLE9BQU8sSUFBSSxNQUFNLEtBQUssSUFBSSxHQUFHLEtBQUssSUFBSSxDQUFDO0NBQ3pDO0FBQ0Y7OztBQ0pBLFNBQWdCLEtBQUssR0FBVSxHQUFrQjtDQUUvQyxPQURlLEVBQUUsTUFBTSxDQUNYO0FBQ2QifQ==
