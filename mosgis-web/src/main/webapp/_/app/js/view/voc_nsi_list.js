
define ([], function () {
    
    return function (data, view) {           
        
        var topmost_layout = w2ui ['topmost_layout']
                
        topmost_layout.unlock ('main')
                
        var layout = $(topmost_layout.el ('main')).w2relayout ({

            name: 'vocs_layout',

            panels: [
                {type: 'left', size: 300, resizable: true},
                {type: 'main', size: 400},                
            ],            
            
        });

        $(layout.el ('left')).empty ().w2residebar ({
            
            name: 'sidebar',
            nodes: data.vc_nsi_list_group,
            onClick: $_DO.open_nsi_list,

        })

    }

});