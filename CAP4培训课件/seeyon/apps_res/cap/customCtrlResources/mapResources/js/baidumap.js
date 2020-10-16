var oldPlace = window.parentDialogObj['dialog'].getTransParams().oldPlace;
var map = new BMap.Map("allmap");
var geoc = new BMap.Geocoder();
var point;
var needlocation = true;
map.addEventListener("click", holdon);
if (oldPlace != "" && oldPlace.indexOf("|" > 0)) {
	var arr = oldPlace.split("|");
	document.getElementById("area").value = arr[0];
	arr = arr[1].split(",");
	point = new BMap.Point(arr[0], arr[1]);
	var marker = new BMap.Marker(new BMap.Point(point.lng,
			point.lat)); // 创建点
	map.addOverlay(marker); // 增加点
	marker.setAnimation(BMAP_ANIMATION_BOUNCE); // 跳动的动画
	// 无需定位了
	needlocation = false;
} else {
	point = new BMap.Point(116.331398, 39.897445);
}
// 百度地图API功能
map.centerAndZoom(point, 16);
var navigationControl = new BMap.NavigationControl({
	// 靠左上角位置
	anchor : BMAP_ANCHOR_TOP_LEFT,
	// LARGE类型
	type : BMAP_NAVIGATION_CONTROL_LARGE,
	// 启用显示定位
	enableGeolocation : true
});

var geolocationControl = new BMap.GeolocationControl();

geolocationControl.addEventListener("locationSuccess", function(e){
    // 定位成功事件
	var addComp = e.addressComponent;
	console.log(addComp);
	position = addComp.province + addComp.city + addComp.district
		+ addComp.street + addComp.streetNumber + "|" + e.point.lng
		+ ',' + e.point.lat;
	document.getElementById("area").value = position;
  });

geolocationControl.addEventListener("locationError",function(e){
    // 定位失败事件
    alert(e.message);
  });

map.addControl(geolocationControl);

map.addControl(navigationControl);
map.addControl(new BMap.NavigationControl()); // 添加控件：缩放地图的控件，默认在左上角；
map.addControl(new BMap.MapTypeControl()); // 添加控件：地图类型控件，默认在右上方；
map.addControl(new BMap.ScaleControl()); // 添加控件：地图显示比例的控件，默认在左下方；
map.addControl(new BMap.OverviewMapControl()); // 添加控件：地图的缩略图的控件，默认在右下方；
// TrafficControl
map.enableScrollWheelZoom(); // 启用滚轮放大缩小，默认禁用
map.enableContinuousZoom(); // 启用地图惯性拖拽，默认禁用

/*
 * var local = new BMap.LocalSearch(map, { renderOptions: { map: map } });
 * 
 * local.search("航兴国际");
 */
if(needlocation) {
	var geolocation = new BMap.Geolocation();
	geolocation.getCurrentPosition(function(r) {
		if (this.getStatus() == BMAP_STATUS_SUCCESS) {
			var mk = new BMap.Marker(r.point);
			map.addOverlay(mk);
			map.panTo(r.point);
			
			var pt = r.point;
			geoc.getLocation(pt, function(rs) {
				var addComp = rs.addressComponents;
				position = addComp.province + addComp.city + addComp.district
				+ addComp.street + addComp.streetNumber + "|" + r.point.lng
				+ ',' + r.point.lat;
				document.getElementById("area").value = position;
				
				map.removeEventListener("click", holdon);
				
				// 定位成功后添加点击事件
				map.addEventListener("click", function(e) {
					map.removeOverlay(map.getOverlays()[0]);
					var marker = new BMap.Marker(new BMap.Point(e.point.lng,
							e.point.lat)); // 创建点
					map.addOverlay(marker); // 增加点
					marker.setAnimation(BMAP_ANIMATION_BOUNCE); // 跳动的动画
					var pt = e.point;
					geoc.getLocation(pt, function(rs) {
						console.log(rs);
						var addComp = rs.addressComponents;
						position = addComp.province + addComp.city
						+ addComp.district + addComp.street
						+ addComp.streetNumber + "|" + rs.point.lng + ','
						+ rs.point.lat;
						document.getElementById("area").value = position;
					});
				});
				
			});
		} else {
			alert('failed' + this.getStatus());
		}
	}, {
		enableHighAccuracy : true
	})
} else {
	map.removeEventListener("click", holdon);
	// 定位成功后添加点击事件
	map.addEventListener("click", function(e) {
		var overlays = map.getOverlays();
		
		map.clearOverlays();
		
		var marker = new BMap.Marker(new BMap.Point(e.point.lng,
				e.point.lat)); // 创建点
		map.addOverlay(marker); // 增加点
		marker.setAnimation(BMAP_ANIMATION_BOUNCE); // 跳动的动画
		var pt = e.point;
		geoc.getLocation(pt, function(rs) {
			var addComp = rs.addressComponents;
			position = addComp.province + addComp.city
			+ addComp.district + addComp.street
			+ addComp.streetNumber + "|" + rs.point.lng + ','
			+ rs.point.lat;
			document.getElementById("area").value = position;
		});
	});
}

function holdon() {
	alert("请等待定位结束后再点击");
}

function mapclick() {
	
}

function search() {
	var value = document.getElementById("area").value;
	var local = new BMap.LocalSearch(map, {
		renderOptions : {
			map : map
		}
	});
	local.search(value);
}