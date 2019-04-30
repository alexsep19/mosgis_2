define ([], function () {

    return function (data, view) {
        
        $((w2ui ['voc_organization_legal_layout']).el ('main')).w2regrid ({

            name: 'houses_grid',

            show: {
                toolbar: true,
                footer: true,
                toolbarInput: true,
            },            

            columns: [                
                {field: 'is_condo',   caption: 'Тип дома', size: 10, render: function (r) {return r.is_condo ? 'МКД' : 'ЖД'}},
                {field: 'fiashouseguid', caption: 'GUID ФИАС',    size: 30},
                {field: 'address', caption: 'Адрес',    size: 50},
                {field: 'id_status', caption: 'Статус', size: 10, voc: data.vc_house_status},
            ],
            
            postData: {data: {uuid_org: $_REQUEST.id}},

            url: '/_back/?type=houses',
            
            onDblClick: function (e) {openTab ('/house/' + e.recid)}

        }).refresh ();

        $('#grid_houses_grid_search_all').focus ()

    }

})