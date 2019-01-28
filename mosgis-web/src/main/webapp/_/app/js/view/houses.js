define ([], function () {

    return function (data, view) {
        
        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({ 

            name: 'houses_grid',             

            show: {
                toolbar: true,
                footer: true,
            },     
            
            searches: [
                {field: 'is_condo',            caption: 'Тип',           type: 'list', options: {items: [
                    {id: "1", text: 'МКД'},
                    {id: "0", text: 'ЖД'},
                ]}},
                {field: 'address_uc',           caption: 'Адрес',         type: 'text'},
                {field: 'fiashouseguid',           caption: 'GUID ФИАС',         type: 'text', operators: ['null', 'not null', 'is'], operator: 'is'},
            ],

            columns: [                
                {field: 'is_condo',   caption: 'Тип дома', size: 10, render: function (r) {return r.is_condo ? 'МКД' : 'ЖД'}},
//                {field: 'unom', caption: 'UNOM',    size: 8},
                {field: 'fiashouseguid', caption: 'GUID ФИАС',    size: 30},
                {field: 'address', caption: 'Адрес',    size: 50},
                {field: 'oktmo', caption: 'ОКТМО', size: 15, render: function (r) { return r['vc_buildings.oktmo'] }},
                {field: 'status_label', caption: 'Статус', size: 10, render: function (r) {
                    return data.vc_house_status[r.id_status] + (r.id_status_gis ? " - " + data.vc_gis_status[r.id_status_gis] : "")
                }},
                {field: 'code_vc_nsi_24', caption: 'Состояние', size: 20, voc: data.vc_nsi_24}
            ],

            url: '/mosgis/_rest/?type=houses',
            
            onDblClick: function (e) {
                openTab ('/house/' + e.recid)
            }

        }).refresh ();

        $('#grid_houses_grid_search_all').focus ()

    }

})