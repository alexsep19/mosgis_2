define ([], function () {

	$_DO.create_infrastructure_resource = function (e) {
        $_SESSION.set ('record', {})
        use.block ('infrastructure_resources_new')
    }

    return function (done) {        
        
        var layout = w2ui ['topmost_layout']
            
        if (layout) layout.unlock ('main')

        var data = clone ($('body').data ('data'))

    	var vc_nsi_2_filtered = {}

        data.vc_nsi_3.items.filter (x => data.item.codes_nsi_3.indexOf (x.id) >= 0)
        				   .forEach ((value, index, array) => {
        				   		
        				   		var code = value.nsi_2
        				   		vc_nsi_2_filtered[code] = data.vc_nsi_2[value.nsi_2]

        				   })

        vc_nsi_2_filtered.items = data.vc_nsi_2.items.filter (x => Object.keys (vc_nsi_2_filtered).find (y => x.id == y))

     	data.vc_nsi_2_filtered = vc_nsi_2_filtered
        
        done (data)
        
    }
    
})