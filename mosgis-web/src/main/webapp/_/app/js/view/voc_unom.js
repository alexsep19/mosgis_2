define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['integration_layout'].el ('main')).w2regrid ({ 

            name: 'voc_organizations_grid',

            show: {
                toolbar: true,
                toolbarInput: true,
                footer: true,
            },     

            columns: [                
                {field: 'unom', caption: 'UNOM',    size: 10},
                {field: 'fias', caption: 'ФИАС (исходный)',    size: 36},
                {field: 'fiashouseguid', caption: 'ФИАС (уточнённый)',    size: 36},
                {field: 'b.label', caption: 'Адрес',    size: 50},
                {field: 'h.is_condo', caption: 'МКД/ЖД', size: 10, voc: {1: 'МКД', 0: 'ЖД'}},
                {field: 'kladr', caption: 'КЛАДР',    size: 10},
                {field: 'kad_n', caption: 'Кадастр',    size: 10},
            ],
            
            postData: {data: {uuid_org: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=voc_unom',
                        
//            onDblClick: function (e) {openTab ('/voc_unom/' + e.recid)},

        }).refresh ();

    }

})