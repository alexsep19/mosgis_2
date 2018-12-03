define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({

            name: 'voc_organization_legal_territories_grid',

            show: {
                toolbar: true,
                //toolbarSearch: true,
            },

            columns: [                
                {field: 'oktmo', caption: 'Код', size: 7},
                {field: 'label', caption: 'Наименование территории', size: 50},
            ],
            
            url: '/mosgis/_rest/?type=voc_organization_territories',

        }).refresh ();

    }

})