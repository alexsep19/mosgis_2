define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'property_document_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },     

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 5, caption: 'Значения полей'},
            ], 
            
            columns: [

                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},

                {field: 'prc', caption: 'Доля, %', size: 10},
                {field: 'id_type', caption: 'Документ', size: 25, voc: data.vc_prop_doc_types},
                {field: 'no', caption: '№', size: 25},
                {field: 'dt', caption: 'Дата', size: 18, render:_dt},
                {field: 'is_deleted',  caption: 'Статус',     size: 20, voc: {0: 'Актуально', 1: 'Удалено'}},

            ],

            url: '/_back/?type=property_documents&part=log&id=' + $_REQUEST.id,            

        }).refresh ();

    }

})