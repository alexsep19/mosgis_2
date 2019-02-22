define ([], function () {

    return function (done) {        

        var layout = w2ui ['passport_layout']

        if (layout) layout.unlock ('main')

        var data = $('body').data ('data')

        var it = data.item
        
        data.resources = []
                
        if (it._can.edit) {
        
            var m = 1

            for (var i = 1; i < 6; i ++) {
                
                if (m & it.mask_vc_nsi_2) data.resources.push ({
                    id: i,
                    label: data.vc_nsi_2 [m]
                })

                m = m << 1

            }

        }    
        
        if (data.resources.length == 1) data.resources [0].label = 'Внести показания'
        
        add_vocabularies (data, {
            resources: 1,
        })        
        
        done (data)

    }

})