define ([], function () {

    var b = ['delete', 'undelete']

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['base_decision_msps_grid']

        var t = g.toolbar

        t.disable (b [0])
//        t.disable (b [1])
        
        if (g.getSelection ().length != 1) return

        t.enable (b [g.get (g.getSelection () [0]).is_deleted])

    })}

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({ 

            name: 'base_decision_msps_grid',

            show: {
                toolbar: true,
                toolbarAdd: data._can.create,
                toolbarEdit: data._can.edit,
                footer: true,
            },     

            toolbar: {
            
                items: [
                    {type: 'button', id: b [0], caption: 'Удалить', onClick: $_DO.delete_base_decision_msps, disabled: true, off: !data._can.delete},
                    {type: 'button', id: 'edit', caption: 'Импорт справочника из ГИС ЖКХ', onClick: $_DO.import_base_decision_msps, icon: 'w2ui-icon-pencil', off: $_USER.role.admin}
                ].filter (not_off),
                
            }, 

            searches: [            
                {field: 'label_uc',  caption: 'Наименование',  type: 'text'},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
                {field: 'id_ctr_status', caption: 'Статус синхронизации',     type: 'enum', options: {items: data.vc_gis_status.items}},
            ].filter (not_off),

            columns: [      
            
                {field: 'org.label', caption: 'Организация', size: 100, off: !$_USER.role.admin},
                {field: 'uniquenumber', caption: 'Код в ГИС ЖКХ', size: 20},
                {field: 'decisionname', caption: 'Наименование', size: 50},
                {field: 'code_vc_nsi_301',  caption: 'Тип решения', size: 25, voc: data.vc_nsi_301},
                {field: 'id_ctr_status',  caption: 'Статус', size: 50, voc: data.vc_gis_status},


            ].filter (not_off),
            
            url: '/mosgis/_rest/?type=base_decision_msps',
            
            postData: {data: {uuid_org: $_USER.uuid_org}},
                        
            onAdd:      $_DO.create_base_decision_msps,            
            onEdit:     $_DO.edit_base_decision_msps,
            onDblClick: function (e) {openTab ('/base_decision_msp/' + e.recid)},
            onRefresh: function (e) {e.done (color_data_mandatory)},            
            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

        }).refresh ();

    }

})