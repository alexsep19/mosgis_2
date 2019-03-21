define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 

            name: 'overhaul_regional_program_docs_grid',

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarAdd: data.item._can.edit && ($_USER.role.nsi_20_7 || $_USER.role.admin),
                footer: true,
            },

            searches: [
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
                {field: 'fullname_uc', caption: 'Наименование', type: 'text'},
            ],

            columns: [
                {field: 'code_nsi_79', caption: 'Вид документа', size: 10, voc: data.vc_nsi_79},
                {field: 'number_', caption: 'Номер', size: 10},
                {field: 'date_', caption: 'Дата', size: 10, render: _dt},
                {field: 'fullname', caption: 'Наименование', size: 10},
                {field: 'legislature', caption: 'Орган власти', size: 10},
            ],
            
            url: '/mosgis/_rest/?type=overhaul_regional_program_documents',

            postData: {program_uuid: $_REQUEST.id},

            onAdd: $_DO.create_overhaul_regional_program_doc,
            
            onDblClick: function (e) {
                openTab ('/overhaul_regional_program_doc/' + e.recid)
            },

        }).refresh ();

    }

})