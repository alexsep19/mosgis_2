define ([], function () {

    return function (data, view) {
    
        var is_own = data.item._can.edit
            
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'working_list_common_plans_grid',

            show: {
                toolbar: false,
                footer: true,
            },     
            
            multiSelect: false,
            
            columns: [             
                {field: 'year', caption: 'Год', size: 10},
                {field: 'id_ctr_status', caption: 'Статус', size: 100, voc: data.vc_gis_status},
            ],
            
            records: data.plans,
            
            onDblClick: function (e) {openTab ('/working_plan/' + e.recid)},

        }).refresh ();

    }

})